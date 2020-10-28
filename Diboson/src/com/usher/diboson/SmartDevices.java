package com.usher.diboson;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


// =================================================================================
public class SmartDevices 
{
	// =============================================================================
	// 26/02/2019 ECU General Notes
	//                =============
	//					Kasa devices use port 9999 and this code applies to them
	//                  Eveready (Tuya) devices use port 6668 and do not know yet
	//                  how to handle them
	// 19/04/2019 ECU bit of tidying as a result of using Wireshark to show the information
	//                which the Kasa App transmits when it is trying to discover
	//                devices on the network. Basically it issues the encrypted
	//                COMMAND_INFO
	// 09/05/2019 ECU tidy up the port names
	// 03/10/2019 ECU Tuya with the firmware upgrade to
	//					   WiFi Module:1.1.1
	//                     MCU Module:1.1.1
	//                TUYA_UDP_PORT changed from 6666 to 6667
	// =============================================================================
	private final static String TAG = "SmartDevices";
	// =============================================================================
	
	// =============================================================================
	private static final int 	BRACE_CLOSE = 125;
	private static final int 	BRACE_OPEN  = 123;
	// =============================================================================
	private static final int    DISCOVERY_DELAY			= 5 * 1000;
	private static final int    DISCOVERY_REPEAT_PERIOD	= 10 * 1000;
	// =============================================================================
	public  static final String NAME_KASA				= "TP-Link";
	public  static final String NAME_TUYA				= "Eveready";
	// =============================================================================
	public  static final int 	KASA_TCP_PORT	= 9999;
	public  static final int 	KASA_UDP_PORT	= 9999;
	public  static final int 	TUYA_TCP_PORT	= 6668;	
	public  static final int 	TUYA_UDP_PORT	= 6667; 	
	// =============================================================================
	public	static final int	RELAY_STATE_OFF		= 0;
	public	static final int	RELAY_STATE_ON		= 1;
	public	static final int	RELAY_STATE_UNKNOWN = 2;
	// =============================================================================
	private static final int	TIMEOUT		= 30 * 1000;	// 30 seconds
	// =============================================================================
	// 20/04/2019 ECU declare the make up of the exchanged Tuya packets
	// -----------------------------------------------------------------------------
	// 20/04/2019 ECU the make up of the received data is :-
	// -----------------------------------------------------------------------------
	//private static final int TUYA_PACKET_OFFSET 		=	2;
	// -----------------------------------------------------------------------------
	// 20/04/2019 ECU the following definitions are relative to the above offset
	// -----------------------------------------------------------------------------
	//private static final int TUYA_FRAME_HEADER  		=	0;			// 2 bytes - fixed as 0x55aa
	//private static final int TUYA_VERSION		  		=	2;			// 1 byte
	//private static final int TUYA_COMMAND_WORD  		=	3;			// 1 byte
	//private static final int TUYA_DATA_LENGTH	  		=	4;			// 2 bytes - big endian
	//private static final int TUYA_DATA			 	=	6;			// ....... - start of data
	private static final int TUYA_LENGTH_POSITION		= 	12;			// position of packet length 
	private static final int TUYA_PREFIX_LENGTH			= 	TUYA_LENGTH_POSITION;			
																		// length of the prefix
	private static final int TUYA_SUFFIX_LENGTH			= 	8;			// length of the suffix	
	// =============================================================================
	public static final String COMMAND_SWITCH_ON  = "{\"system\":{\"set_relay_state\":{\"state\":1}}}}";
	public static final String COMMAND_SWITCH_OFF = "{\"system\":{\"set_relay_state\":{\"state\":0}}}}";
	// -----------------------------------------------------------------------------
	// 19/04/2019 ECU changed from '...info\":null}}' not sure why the 'null' was there
	// -----------------------------------------------------------------------------
	public static final String COMMAND_INFO = "{\"system\":{\"get_sysinfo\":{}}}";
	// ============================================================================= 
	public static final int STATE_ON 	= 1;
	public static final int STATE_OFF 	= 2;
	// =============================================================================
	
	// =============================================================================
	SmartDevicesDatagramThread datagramThreadKasa;
	SmartDevicesDatagramThread datagramThreadTuya;
	// =============================================================================
	// 19/04/2019 ECU Note - declare any local variables
	// 10/05/2019 ECU added kasaDevices
	// -----------------------------------------------------------------------------
	private static ArrayList<KasaDevice> 
							kasaDevices = new ArrayList<KasaDevice> ();
	private String 			ipAddress;
	private int 			port = KASA_UDP_PORT;
	private static String	tuyaGwId;
	private static byte []	tuyaPrefix;
	private static String	tuyaProductKey;
	private static byte [] 	tuyaSuffix;
	// =============================================================================
	
	// =============================================================================
	public SmartDevices ()
	{
		// -------------------------------------------------------------------------
		// 19/04/2019 ECU added
		// 05/10/2019 ECU check if the stored ports need to be set up
		// -------------------------------------------------------------------------
		if (PublicData.storedData.smart_device_kasa_tcp_port == 0)
		{
			// ---------------------------------------------------------------------
			// 05/10/2019 ECU the stored data has not been set up so initialise to
			//                the defaults
			// ---------------------------------------------------------------------
			PublicData.storedData.smart_device_kasa_tcp_port = KASA_TCP_PORT;
			PublicData.storedData.smart_device_kasa_udp_port = KASA_UDP_PORT;
			PublicData.storedData.smart_device_tuya_tcp_port = TUYA_TCP_PORT;
			PublicData.storedData.smart_device_tuya_udp_port = TUYA_UDP_PORT;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
	public SmartDevices (String theIpAddress) 
	{
		this.ipAddress = theIpAddress;
	}
	// -----------------------------------------------------------------------------
	public SmartDevices (String theIpAddress, int thePort) 
	{
		this.ipAddress = theIpAddress;
		this.port = thePort;
	}
	// =============================================================================
	
	// =============================================================================
	public String decryptStream (InputStream theInputStream) throws IOException 
	{
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU decrypt the characters that are received on the specified
		//                stream
		// 19/04/2019 ECU changed to 'public'
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU initialise the local key and declare the next key to be
		//                generated
		// -------------------------------------------------------------------------
		int localKey = 0x2B;
		int localNextKey;
		// -------------------------------------------------------------------------
		int inputCharacter;
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU declare the buffer that will contain the decrypted characters
		// -------------------------------------------------------------------------
		StringBuilder decryptedBuffer = new StringBuilder();
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU the received text is delimited by { and } so use these to
		//                terminate the input rather than waiting for a time out
		// -------------------------------------------------------------------------
		int braceCounter = 1;					// the 1 is because there is an implied
												// leading '{'
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU read in the characters
		// -------------------------------------------------------------------------
		while((inputCharacter = theInputStream.read()) != -1) 
		{
			// ---------------------------------------------------------------------
			// 26/02/2019 ECU remember this character as the next key that will be
			//                used
			// ---------------------------------------------------------------------
			localNextKey = inputCharacter;
			// ---------------------------------------------------------------------
			// 26/02/2019 ECU decrypt the character by XORing with current key
			// ---------------------------------------------------------------------
			inputCharacter = inputCharacter ^ localKey;
			// ---------------------------------------------------------------------
			// 26/02/2019 ECU reset the key with that stored
			// ---------------------------------------------------------------------
			localKey = localNextKey;
			// ---------------------------------------------------------------------
			// 26/02/2019 ECU add the decrypted character into the buffer
			// ---------------------------------------------------------------------
			decryptedBuffer.append((char) inputCharacter);
			// ---------------------------------------------------------------------
			// 26/02/2019 ECU check the pairing of the braces
			// ---------------------------------------------------------------------
			if (inputCharacter == BRACE_OPEN)
				braceCounter++;
			else
			if (inputCharacter == BRACE_CLOSE)
				braceCounter--;
			// ---------------------------------------------------------------------
			// 26/02/2019 ECU now check if the syntax is complete - if so break out
			//                of the loop immediately
			// ---------------------------------------------------------------------
			if (braceCounter == 0)
				 break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU now return the decrypted buffer in the correct format
		// 19/04/2019 ECU changed the substring from '5' to '1'
		// -------------------------------------------------------------------------
		return "{" + decryptedBuffer.toString().substring (1);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void deviceUpdate (String theName,String theIPAddress,int thePort,String theData)
	{
		// -------------------------------------------------------------------------
		// 26/04/2019 ECU check if the device exists - if not then create an entry
		//                - if it does exist then update the existing entry
		// 27/04/2019 ECU passed through the data associated with the received packet
		// -------------------------------------------------------------------------
		// 26/04/2019 ECU create a 'local device' and copy across some details
		// 27/04/2019 ECU added the storage of the data and the setting of the
		//                smartDevice flag
		// -------------------------------------------------------------------------
		Devices localDevice 	= new Devices ();
		localDevice.IPAddress 	= theIPAddress;
		localDevice.compatible 	= false;
		localDevice.name		= theName;
		localDevice.response	= theData;
		localDevice.smartDevice	= true;
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.deviceDetails.size(); theIndex++)
			{
				if (PublicData.deviceDetails.get (theIndex).IPAddress.equalsIgnoreCase (theIPAddress))
				{
					// -------------------------------------------------------------
					// 26/04/2019 ECU the IP address has been found
					// -------------------------------------------------------------
					PublicData.deviceDetails.set (theIndex,localDevice);
					// -------------------------------------------------------------
					// 26/04/2019 ECU just exit
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 26/04/2019 ECU there is no entry so add the device details
		// -------------------------------------------------------------------------
		PublicData.deviceDetails.add (localDevice);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public int [] encrypt (String theString) 
    {	 
    	// -------------------------------------------------------------------------
    	// 26/02/2019 ECU the encryption is a trivial XOR autokey encryption - this
    	//                does not really provide any security
    	// 19/04/2019 ECU made 'public'
    	// -------------------------------------------------------------------------
    	// 26/02/2019 ECU initialise the key
    	// -------------------------------------------------------------------------
    	int localKey = 0xAB;
    	// -------------------------------------------------------------------------
    	// 26/02/2019 ECU create a buffer which will contain the encrypted characters
    	// -------------------------------------------------------------------------
        int [] localBuffer = new int [theString.length()];
        // -------------------------------------------------------------------------
        // 26/02/2019 ECU now loop through all characters in the string
        // -------------------------------------------------------------------------
        for (int index = 0; index < theString.length(); index++) 
        {
        	// ---------------------------------------------------------------------
        	// 26/02/2019 ECU XOR the character at indexed point with the current key
        	// ---------------------------------------------------------------------
            localBuffer [index] = theString.charAt (index) ^ localKey;
            // ---------------------------------------------------------------------
            // 26/02/2019 ECU now reset the key to the newly encrypted character
            // ---------------------------------------------------------------------
            localKey = localBuffer [index];
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 26/02/2019 ECU return the encrypted buffer
        // -------------------------------------------------------------------------
        return localBuffer;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private byte [] encryptWithHeader (String theString) 
    {
    	// -------------------------------------------------------------------------
    	// 20/04/2019 ECU Note - initially encrypt the data
    	// -------------------------------------------------------------------------
        int [] localData = encrypt (theString);
        // -------------------------------------------------------------------------
        // 20/04/2019 ECU Note - allocate 4 bytes and then store the length of the data
        //                       into it
        // -------------------------------------------------------------------------
        byte [] bufferHeader = ByteBuffer.allocate (4).putInt (theString.length ()).array ();
        // -------------------------------------------------------------------------
        // 20/04/2019 ECU Note - now copy across the data after allocating enough
        //                       room in the buffer
        // -------------------------------------------------------------------------
        ByteBuffer byteBuffer = ByteBuffer.allocate (bufferHeader.length + localData.length).put (bufferHeader);
        for(int inChar : localData) 
        {
            byteBuffer.put ((byte) inChar);
        }
        // -------------------------------------------------------------------------
        // 20/04/2019 ECU Note - now return the encrypted data
        // -------------------------------------------------------------------------
        return byteBuffer.array();
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	public Map <String,String> getInfo() throws IOException 
	{
		// -------------------------------------------------------------------------
		// 27/02/2019 ECU return the information from this device
		//                The calling method can handle the data as follows
		//                      Map<String,String>info = <instance>.getInfo();
		//              		Iterator <Entry<String, String>> iter = info.entrySet().iterator();
		//       				while (iter.hasNext()) 
		//						{
		//    						Entry<String, String> entry = iter.next();
		//  
		//    						get key   using entry.getKey()
		//                          get value using entry.getValue()
		//                      }
		// -------------------------------------------------------------------------
		// 27/02/2019 ECU request the information
		// -------------------------------------------------------------------------
		String informationData = sendCommand (COMMAND_INFO);
		// -------------------------------------------------------------------------
		// 27/02/2019 ECU declare the map that will contain the data which is a list
		//                of pairs as <key>:<value>,....
		// -------------------------------------------------------------------------
		Map <String,String> deviceInformation = new HashMap <String, String>();
		// -------------------------------------------------------------------------
		// 27/02/2019 ECU check that there is data to analyse
		// -------------------------------------------------------------------------
		if (informationData.length() > 0) 
		{
			try
			{
				// -----------------------------------------------------------------
				// 27/02/2019 ECU get the information from the response
				// -----------------------------------------------------------------
				JSONObject sysInfo  = (new JSONObject (informationData)).getJSONObject ("system").getJSONObject ("get_sysinfo");
				// -----------------------------------------------------------------
				// 27/02/2019 ECU get information about the keys
				// -----------------------------------------------------------------
				Iterator<String> localInfoKeys = sysInfo.keys ();
				// -----------------------------------------------------------------
				// 27/02/2019 ECU now loop through the keys building up the 'map'
				//                which will be returned
				// -----------------------------------------------------------------
	            while (localInfoKeys.hasNext())
	            {
	            	// -------------------------------------------------------------
	            	// 27/02/2019 ECU get the next key
	            	// -------------------------------------------------------------
	                String localKey= localInfoKeys.next();	
	                // -------------------------------------------------------------
	                // 27/02/2019 ECU add an entry to the map
	                //                    key,value
	                // -------------------------------------------------------------
	                deviceInformation.put (localKey,sysInfo.get(localKey).toString());
	                // -------------------------------------------------------------
	            }
			}
			catch (Exception theException)
			{	
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/02/2019 ECU return the entries in the map
		// -------------------------------------------------------------------------
		return deviceInformation;
		// -------------------------------------------------------------------------
	}	
	// =============================================================================
	public String getIpAddress ()
	{
		return this.ipAddress;
	}
	// =============================================================================
	public static String getJSONKey (String theJSONData,String theKey)
	{
		// -------------------------------------------------------------------------
		// 10/05/2019 ECU create to obtain the specified key from the JSON data
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 10/05/2019 ECU get the object which contains the relay state
			// ---------------------------------------------------------------------
			JSONObject jsonObject = (new JSONObject (theJSONData)).getJSONObject ("system").getJSONObject ("get_sysinfo");
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU now return the retrieved state
			// ---------------------------------------------------------------------
			return jsonObject.getString (theKey);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 10/05/2019 ECU an exception occurred so report the fact
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static int getJSONRelayState (String theJSONData)
	{
		// -------------------------------------------------------------------------
		// 30/04/2019 ECU create to obtain the relay state from the JSON data
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU get the object which contains the relay state
			// ---------------------------------------------------------------------
			JSONObject jsonObject = (new JSONObject (theJSONData)).getJSONObject ("system").getJSONObject ("get_sysinfo");
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU now return the retrieved state
			// ---------------------------------------------------------------------
			return jsonObject.getInt ("relay_state");
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU an exception occurred so report the fact
			// ---------------------------------------------------------------------
			return RELAY_STATE_UNKNOWN;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/04/2019 ECU created to initialise the smart device system
		// -------------------------------------------------------------------------
		// 09/05/2019 ECU use the new port names
		// 05/10/2019 ECU change to use the stored ports
		// -------------------------------------------------------------------------
		datagramThreadKasa = listenForDevices (theContext,NAME_KASA,PublicData.storedData.smart_device_kasa_udp_port,TIMEOUT);
		datagramThreadTuya = listenForDevices (theContext,NAME_TUYA,PublicData.storedData.smart_device_tuya_udp_port,TIMEOUT);
		// -------------------------------------------------------------------------
		// 20/04/2019 ECU send out any broadcast packets
		// 30/04/2019 ECU added the repeat rate as the second argument (in milliseconds)
		// 10/05/2019 ECU added the initial delay
		// --------------------------------------------------------------------------
		sendDiscoveryCommandKasa (SmartDevices.COMMAND_INFO,DISCOVERY_DELAY,DISCOVERY_REPEAT_PERIOD);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public void initiateImmediateRefresh ()
	{
		// -------------------------------------------------------------------------
		// 10/05/2019 ECU create to force an immediate refresh
		// -------------------------------------------------------------------------
		sendDiscoveryCommandKasa (SmartDevices.COMMAND_INFO,100,DISCOVERY_REPEAT_PERIOD);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean isPresent () 
	{
		// -------------------------------------------------------------------------
		// 27/02/2019 ECU checks if the defined device exists
		// -------------------------------------------------------------------------
		try 
		{
			InetAddress ip = InetAddress.getByName (getIpAddress ());
			return ip.isReachable(500);
		} 
		catch (IOException ex) 
		{
			
		}
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	SmartDevicesDatagramThread listenForDevices (Context theContext,String theName,int thePort,int theTimeout)
	{
		// -------------------------------------------------------------------------
		// 04/04/2019 ECU create the thread that will listen for packets
		// 20/04/2019 ECU added the name which is passed through to the thread
		// 02/05/2019 ECU added the timeout as an argument. This is the time in
		//                milliseconds that will wait for a packet
		// -------------------------------------------------------------------------
		final SmartDevicesDatagramThread listenThread = new SmartDevicesDatagramThread (theContext,theName,thePort,theTimeout);
		// -------------------------------------------------------------------------
		// 04/04/2019 ECU start up the thread
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		{
			@Override
			public void run()
		    {
				listenThread.run ();              
		    }
		};
		// -------------------------------------------------------------------------
		thread.start();   
		// -------------------------------------------------------------------------
		// 20/04/2019 ECU return the thread
		// -------------------------------------------------------------------------
		return listenThread;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void processIncomingPacket (int thePort,String theName,String theAddress,byte [] theBytes)
	{
		// -------------------------------------------------------------------------
		// 19/04/2019 ECU process a message from a smart device
		// 20/04/2019 ECU added the name of the originator
		// -------------------------------------------------------------------------
		// 20/04/2019 ECU IMPORTANT - broadcast messages sent by this device will
		//                            also be received by this device - there is no
		//                            need to process these
		// -------------------------------------------------------------------------
		if (theAddress.equalsIgnoreCase (PublicData.ipAddress))
		{
			// ---------------------------------------------------------------------
			// 20/04/2019 ECU this packet is for this device so ignore it
			// ---------------------------------------------------------------------
			return;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 19/04/2019 ECU decrypt the received data
			// 20/04/2019 ECU at the moment only decrypt 'Kasa' messages
			// ---------------------------------------------------------------------
			String decryptedData = StaticData.BLANK_STRING;
			
			if (theName.equals (NAME_KASA))
			{
				// -----------------------------------------------------------------
				// 19/04/2019 ECU change the byte [] to an input stream
				// -----------------------------------------------------------------
				InputStream inputStream = new ByteArrayInputStream (theBytes);
				// -----------------------------------------------------------------
				decryptedData = decryptStream (inputStream);
				// -----------------------------------------------------------------
				// 30/04/2019 ECU now try and extract the JSON information from the
				//                received data
				// 10/05/2019 ECU change to use the new class
				// -----------------------------------------------------------------
				new KasaDevice (decryptedData);
				// -----------------------------------------------------------------
			}
			else
			if (theName.equals(NAME_TUYA))
			{
				decryptedData = this.TuyaDecode (theBytes);
			}
			// ---------------------------------------------------------------------
			// 19/04/2019 ECU at the moment just log the data
			// 20/04/2019 ECU tidied up and added the originator
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Type : " + theName + "  Source : " + theAddress + "  Port : " + thePort + "   " + decryptedData);
			// ---------------------------------------------------------------------
			// 26/04/2019 ECU add a 'device' if it does not already exit
			// 27/04/2019 ECU added the decrypted data
			// ---------------------------------------------------------------------
			deviceUpdate (theName,theAddress,thePort,decryptedData);
			// ---------------------------------------------------------------------
			// 26/04/2019 ECU get the display refreshed
			// 28/04/2019 ECU don't want to queue messages so first of all clear
			//                any outstanding REFRESH messages
			// ---------------------------------------------------------------------
			DevicesActivity.refreshHandler.removeMessages  (StaticData.MESSAGE_REFRESH);
			DevicesActivity.refreshHandler.sendEmptyMessage(StaticData.MESSAGE_REFRESH);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
		
	}
	// =============================================================================
	int readIntFromByteArray (byte[] theBuffer, ByteOrder theEndian) 
	{
		// -------------------------------------------------------------------------
		// 08/05/2019 ECU converts the byte array into an integer assuming that
		//                it is stored as 'big endian'
		// -------------------------------------------------------------------------
	    final ByteBuffer localBuffer = ByteBuffer.wrap (theBuffer);
	    // -------------------------------------------------------------------------
	    // 08/05/2019 ECU need to rearrange the buffer depending on little or big
	    //                endian
	    // -------------------------------------------------------------------------
	    localBuffer.order (theEndian);
	    // -------------------------------------------------------------------------
	    // 08/05/2019 ECU now return the generate integer
	    // -------------------------------------------------------------------------
	    return localBuffer.getInt ();
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	protected boolean returnErrorState (String theString)
	{
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU create to return the error state based on the JSON string
		//                that is being parsed
		//						true  = no error
		//					    false = error occurred
		// -------------------------------------------------------------------------
		if (theString.length() > 0) 
    	{
    		try 
    		{
    			// -----------------------------------------------------------------
    			// 26/02/2019 ECU convert the string to its JSON object
    			// -----------------------------------------------------------------
    			JSONObject jo = new JSONObject (theString);
    			// -----------------------------------------------------------------
    			// 26/02/2019 ECU get the error code from the data
    			// -----------------------------------------------------------------
    			int errorCode = jo.getJSONObject ("system").getJSONObject ("set_relay_state").getInt ("err_code");
    			// -----------------------------------------------------------------
    			// 26/02/2019 ECU convert the code to the boolean to be returned
    			// -----------------------------------------------------------------
 	            return errorCode == 0;
 	            // -----------------------------------------------------------------
			} 
    		catch (JSONException theException) 
    		{
			}  
    		// ---------------------------------------------------------------------
	    }
		// -------------------------------------------------------------------------
		// 26/02/2019 ECU problem with the response so indicate an error
		// -------------------------------------------------------------------------
	    return false;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
    protected String sendCommand (String command) throws IOException 
    { 
    	// -------------------------------------------------------------------------
        Socket socket = new Socket (getIpAddress (),port);
        OutputStream outputStream = socket.getOutputStream ();
        outputStream.write (encryptWithHeader(command));
        // -------------------------------------------------------------------------
        InputStream inputStream = socket.getInputStream ();
        String data = decryptStream (inputStream);
        // -------------------------------------------------------------------------
        outputStream.close ();
        inputStream.close ();
        socket.close ();
        // -------------------------------------------------------------------------
        return data;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void sendDiscoveryCommandKasa (String theCommand,int theInitialDelay,int theRepeatTime)
    {
    	// -------------------------------------------------------------------------
    	// 20/04/2019 ECU created to send a broadcast message to the Kasa devices
    	// 30/04/2019 ECU added the repeat time argument in milliseconds
    	// 10/05/2019 ECU added the initial delay
    	// -------------------------------------------------------------------------
    	try
		{
			int [] command = encrypt (theCommand);
			ByteBuffer commandBuffer = ByteBuffer.allocate (command.length);
			for (int inChar : command) 
			{
				commandBuffer.put ((byte) inChar);
			}
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU indicate that the packet is to be sent repeatedly
			// ---------------------------------------------------------------------
			datagramThreadKasa.broadcastPacketRepeat = theRepeatTime;
			// ---------------------------------------------------------------------
			// 09/05/2019 ECU changed to use new port name
			// 05/10/2019 ECU changed to use the stored port
			// ---------------------------------------------------------------------
			datagramThreadKasa.broadcastPacket = new DatagramPacket (commandBuffer.array(), 
												  commandBuffer.array().length,
												  InetAddress.getByName ("255.255.255.255"),
												  PublicData.storedData.smart_device_kasa_udp_port);
			// ---------------------------------------------------------------------
			// 10/05/2019 ECU make sure that there are no queued messages
			// ---------------------------------------------------------------------
			datagramThreadKasa.broadcastRefreshHandler.removeMessages (StaticData.MESSAGE_SEND);
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU now get the message transmitted after an initial delay
			// ----------------------------------------------------------------------
			datagramThreadKasa.broadcastRefreshHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_SEND,theInitialDelay);
			// ----------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public boolean switchOn() throws IOException 
    { 
    	// -------------------------------------------------------------------------
    	// 26/02/2019 ECU created to cause the device to be switched on
    	// -------------------------------------------------------------------------
    	String jsonData = sendCommand (COMMAND_SWITCH_ON);
    	// -------------------------------------------------------------------------
    	// 27/02/2019 ECU return with the correct state
    	// -------------------------------------------------------------------------
    	return returnErrorState (jsonData);
    	//--------------------------------------------------------------------------
    }
	// =============================================================================
    public boolean switchOff() throws IOException 
    {
    	// -------------------------------------------------------------------------
    	// 26/02/2019 ECU created to cause the device to be switched off
    	// -------------------------------------------------------------------------
    	String jsonData = sendCommand (COMMAND_SWITCH_OFF);
    	// -------------------------------------------------------------------------
    	// 27/02/2019 ECU return with the correct state
    	// -------------------------------------------------------------------------
    	return returnErrorState (jsonData);
    	//--------------------------------------------------------------------------
    }
    // =============================================================================
    void terminate ()
    {
    	// -------------------------------------------------------------------------
    	// 20/04/2019 ECU created to handle the termination of the Smart Devices
    	//                system
    	// -------------------------------------------------------------------------
    	// 20/04/2019 ECU stop the listening threads
    	// 30/04/2019 ECU changed the way the thread is closed
    	//            ECU added the try/catch in case of NPE
    	// -------------------------------------------------------------------------
    	try
    	{
    		datagramThreadKasa.broadcastRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
    		datagramThreadTuya.broadcastRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
    	}
    	catch (Exception theException)
    	{
    		
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static byte [] tuyaCommand (String theCommand)
    {
    	String JSONHeader = StaticData.BLANK_STRING;
    	try
    	{
    		JSONObject json = new JSONObject ();
    		json.put ("gwId", tuyaGwId);
    		json.put ("productKey",tuyaProductKey);
    		
    		JSONHeader = json.toString();
    	}
    	catch (Exception theException)
    	{
    		
    	}
    	
    	theCommand = JSONHeader + theCommand;
    	// -------------------------------------------------------------------------
    	// 25/04/2019 ECU created to return the command surrounded by prefix / suffix
    	// -------------------------------------------------------------------------
    	byte[] localOutput = new byte [tuyaPrefix.length + theCommand.length() + tuyaSuffix.length];
    	System.arraycopy (tuyaPrefix, 0,localOutput, 0,tuyaPrefix.length);
    	System.arraycopy (theCommand.getBytes(), 0,localOutput, tuyaPrefix.length,theCommand.length ());
    	System.arraycopy (tuyaSuffix, 0,localOutput, tuyaPrefix.length + theCommand.length (),tuyaSuffix.length);
    	// -------------------------------------------------------------------------
    	return localOutput;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static int TuyaCRC (byte [] theData)
    {
    	// -------------------------------------------------------------------------
    	// 30/04/2019 ECU created to generate a 32 bit CRC in the style used by Tuya
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU initial investigation showed solutions which had the table
    	//                hard coded. However now that I know how to generate it the
    	//                table has been commented out but left here for completion
    	// -------------------------------------------------------------------------
    	//final int [] crc32Table = new int [] { 
    	//		 						0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 	// 00
    	//		 						0x076DC419, 0x706AF48F, 0xE963A535, 0x9E6495A3, 	// 01
    	//		 						0x0EDB8832, 0x79DCB8A4, 0xE0D5E91E, 0x97D2D988, 	// 02
    	//		 						0x09B64C2B, 0x7EB17CBD, 0xE7B82D07, 0x90BF1D91, 	// 03
    	//		 						0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE,     // 04
    	//		 						0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7,     // 05
    	//		 						0x136C9856, 0x646BA8C0, 0xFD62F97A, 0x8A65C9EC,     // 06
    	//		 						0x14015C4F, 0x63066CD9, 0xFA0F3D63, 0x8D080DF5,     // 07
    	//		 						0x3B6E20C8, 0x4C69105E, 0xD56041E4, 0xA2677172,     // 08
    	//		 						0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B,     // 09
    	//		 						0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940,     // 10
    	//		 						0x32D86CE3, 0x45DF5C75, 0xDCD60DCF, 0xABD13D59,     // 11
    	//		 						0x26D930AC, 0x51DE003A, 0xC8D75180, 0xBFD06116,     // 12
    	//		 						0x21B4F4B5, 0x56B3C423, 0xCFBA9599, 0xB8BDA50F,     // 13
    	//		 						0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924,     // 14
    	//		 						0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D,     // 15
    	//		 						0x76DC4190, 0x01DB7106, 0x98D220BC, 0xEFD5102A,     // 16
    	//		 						0x71B18589, 0x06B6B51F, 0x9FBFE4A5, 0xE8B8D433,     // 17
    	//		 						0x7807C9A2, 0x0F00F934, 0x9609A88E, 0xE10E9818,     // 18
    	//		 						0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01,     // 19
    	//		 						0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E,     // 20
    	//		 						0x6C0695ED, 0x1B01A57B, 0x8208F4C1, 0xF50FC457,     // 21
    	//		 						0x65B0D9C6, 0x12B7E950, 0x8BBEB8EA, 0xFCB9887C,     // 22
    	//		 						0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3, 0xFBD44C65,     // 23
    	//		 						0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2,     // 24
    	//		 						0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB,     // 25
    	//		 						0x4369E96A, 0x346ED9FC, 0xAD678846, 0xDA60B8D0,     // 26
    	//		 						0x44042D73, 0x33031DE5, 0xAA0A4C5F, 0xDD0D7CC9,     // 27
    	//		 						0x5005713C, 0x270241AA, 0xBE0B1010, 0xC90C2086,     // 28
    	//		 						0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,     // 29
    	//		 						0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4,     // 30
    	//		 						0x59B33D17, 0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD,     // 31
    	//		 						0xEDB88320, 0x9ABFB3B6, 0x03B6E20C, 0x74B1D29A,     // 32
    	//		 						0xEAD54739, 0x9DD277AF, 0x04DB2615, 0x73DC1683,     // 33
    	//		 						0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8,     // 34
    	//		 						0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1,     // 35
    	//		 						0xF00F9344, 0x8708A3D2, 0x1E01F268, 0x6906C2FE,     // 36
    	//		 						0xF762575D, 0x806567CB, 0x196C3671, 0x6E6B06E7,     // 37
    	//		 						0xFED41B76, 0x89D32BE0, 0x10DA7A5A, 0x67DD4ACC,     // 38
    	//		 						0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5,     // 39
    	//		 						0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252,     // 40
    	//		 						0xD1BB67F1, 0xA6BC5767, 0x3FB506DD, 0x48B2364B,     // 41
    	//		 						0xD80D2BDA, 0xAF0A1B4C, 0x36034AF6, 0x41047A60,     // 42
    	//		 						0xDF60EFC3, 0xA867DF55, 0x316E8EEF, 0x4669BE79,     // 43
    	//		 						0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236,     // 44
    	//		 						0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F,     // 45
    	//		 						0xC5BA3BBE, 0xB2BD0B28, 0x2BB45A92, 0x5CB36A04,     // 46
    	//		 						0xC2D7FFA7, 0xB5D0CF31, 0x2CD99E8B, 0x5BDEAE1D,     // 47
    	//		 						0x9B64C2B0, 0xEC63F226, 0x756AA39C, 0x026D930A,     // 48
    	//		 						0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713,     // 49
    	//		 						0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38,     // 50
    	//		 						0x92D28E9B, 0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21,     // 51
    	//		 						0x86D3D2D4, 0xF1D4E242, 0x68DDB3F8, 0x1FDA836E,     // 52
    	//		 						0x81BE16CD, 0xF6B9265B, 0x6FB077E1, 0x18B74777,     // 53
    	//		 						0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C,     // 54
    	//		 						0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45,     // 55
    	//		 						0xA00AE278, 0xD70DD2EE, 0x4E048354, 0x3903B3C2,     // 56
    	//		 						0xA7672661, 0xD06016F7, 0x4969474D, 0x3E6E77DB,     // 57
    	//		 						0xAED16A4A, 0xD9D65ADC, 0x40DF0B66, 0x37D83BF0,     // 58
    	//		 						0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,     // 59
    	//		 						0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6,     // 60
    	//		 						0xBAD03605, 0xCDD70693, 0x54DE5729, 0x23D967BF,     // 61
    	//		 						0xB3667A2E, 0xC4614AB8, 0x5D681B02, 0x2A6F2B94,     // 62
    	//		 						0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B, 0x2D02EF8D      // 63
    	// 							};
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU generate the table
    	// -------------------------------------------------------------------------
    	int [] crc32Table = TuyaCRCGenerateTable ();
    	// -------------------------------------------------------------------------
    	// 30/04/2019 ECU preset the crc
    	// -------------------------------------------------------------------------
    	int crc = 0xFFFFFFFF; 
    	// -------------------------------------------------------------------------
    	// 30/04/2019 ECU calculate the CRC using the supplied data
    	// -------------------------------------------------------------------------
    	for (byte localByte : theData) 
    	{
    		crc = (crc >>> 8) ^ crc32Table [(crc ^ localByte) & 255]; 
    	}
    	// -------------------------------------------------------------------------
    	// 30/04/2019 ECU return the CRC
    	// 03/07/2020 ECU the folloing code could be 'return ~crc'
    	// -------------------------------------------------------------------------
    	return crc ^ 0xFFFFFFFF; 
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static int [] TuyaCRCGenerateTable ()
    {
    	//--------------------------------------------------------------------------
    	// 01/05/2019 ECU create to generate the CRC table used when encrypting
    	//                data for transmission
    	//
    	//				  Generate a table for a byte-wise 32-bit CRC calculation on 
    	//				  the polynomial:
    	//					  x^32+x^26+x^23+x^22+x^16+x^12+x^11+x^10+x^8+x^7+x^5+x^4+x^2+x+1.
    	// 
    	//				  Polynomials over GF(2) are represented in binary, one bit 
    	//				  per coefficient, with the lowest powers in the most
    	//				  significant bit.  Then adding polynomials is just exclusive-or, 
    	//				  and multiplying a polynomial by x is a right shift by one.  
    	//				  If we call the above polynomial p, and represent a byte as the
    	//				  polynomial q, also with the lowest power in the most significant
    	//				  bit (so the byte 0xb1 is the polynomial x^7+x^3+x+1), then the 
    	//				  CRC is (q*x^32) mod p, where a mod b means the remainder after 
    	//				  dividing a by b. 
    	//
    	//				  This calculation is done using the shift-register method of 
    	//				  multiplying and taking the remainder.  The register is
    	//				  initialised to zero, and for each incoming bit, x^32 is added 
    	//				  mod p to the register if the bit is a one (where x^32 mod p 
    	//				  is p+x^32 = x^26+...+1), and the register is multiplied mod p by
    	//				  x (which is shifting right by one and adding x^32 mod p if the 
    	//				  bit shifted out is a one).  We start with the highest power 
    	//				  (least significant bit) of q and repeat for all eight bits of q.
    	// 
    	//				  The table is simply the CRC of all possible eight bit values. 
    	//				  This is all the information needed to generate CRC's on data a
    	//				  byte at a time for all combinations of CRC register values and 
    	//				  incoming bytes.
    	// -------------------------------------------------------------------------
    	int [] localCRCTable = new int [256];
    	int 	shiftRegister;
    	int		exclusiveOrPattern;
    	int		byteBeingShifted;
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU polynomial defining this crc (except x^32)
    	// -------------------------------------------------------------------------
    	final int [] polynomial = {0,1,2,4,5,7,8,10,11,12,16,22,23,26};
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU make exclusive-OR pattern from polynomial (0xEDB88320)
    	//										see below
    	//                                      0000000000100000000020000000003
    	//                                      0123456789012345678901234567890
    	//                                      1110110110111000100000110010000
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU initialise the pattern
    	// -------------------------------------------------------------------------
    	exclusiveOrPattern = 0;
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU now loop through the polynomial - bitwise OR
    	// -------------------------------------------------------------------------
    	for (int index = 0; index < polynomial.length; index++)
    	{
    		exclusiveOrPattern |= 1 << (31 - polynomial [index]);
    	}
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU now compute the table
    	// -------------------------------------------------------------------------
    	for (int index = 1; index < 256; index++)
    	{
    		shiftRegister = index;
    		// ---------------------------------------------------------------------
    		// 01/05/2019 ECU the idea to initialise the register with the byte instead
    		//                of zero is taken from Haruhiko Okumura's ar002
    		//            ECU IMPORTANT - the right shift operator '>>>' must be
    		//                            be used instead of '>>' because the later
    		//                            retains the 'sign' bit which is incorrect
    		// ---------------------------------------------------------------------
    		for (byteBeingShifted = 8; byteBeingShifted > 0; byteBeingShifted--)
    		{
    			shiftRegister = ((shiftRegister & 1) == 1) ? (shiftRegister >>> 1) ^ exclusiveOrPattern
    					                                   : (shiftRegister >>> 1);
    		}
    		// ---------------------------------------------------------------------
    		localCRCTable [index] = shiftRegister;
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 01/05/2019 ECU return the generated table
    	// -------------------------------------------------------------------------
    	return localCRCTable;
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
    String TuyaDecode (byte [] theBytes)
    {
    	// -------------------------------------------------------------------------
    	// 20/04/2019 ECU created to show details of the receive packet from the
    	//                Tuya device
    	// -------------------------------------------------------------------------
    	try 
    	{
    		// ---------------------------------------------------------------------
    		// 08/05/2019 ECU read in the prefix data
    		// ---------------------------------------------------------------------
    		tuyaPrefix = Arrays.copyOfRange (theBytes,0,TUYA_PREFIX_LENGTH);
    		// ---------------------------------------------------------------------
    		// 08/05/2019 ECU read in the stored length
    		// ---------------------------------------------------------------------
    		int localPacketLength = readIntFromByteArray (Arrays.copyOfRange (theBytes,TUYA_PREFIX_LENGTH,TUYA_PREFIX_LENGTH + 4),ByteOrder.BIG_ENDIAN);
    		// ---------------------------------------------------------------------
    		// 08/05/2019 ECU read in the suffix data
    		// ---------------------------------------------------------------------
    		tuyaSuffix = Arrays.copyOfRange (theBytes,(theBytes.length - TUYA_SUFFIX_LENGTH),theBytes.length);
    		// ---------------------------------------------------------------------
    		// 08/05/2019 ECU now pull in the actual data
    		// ---------------------------------------------------------------------
			JSONObject json	= new JSONObject (new String (Arrays.copyOfRange (theBytes,TUYA_PREFIX_LENGTH + 4 + 4,(theBytes.length -TUYA_SUFFIX_LENGTH))));
			tuyaGwId 		= json.getString ("gwId");
			tuyaProductKey 	= json.getString ("productKey");
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile(TAG, "Packet Length : " + localPacketLength + StaticData.NEWLINE +
					                        "gwId : " + tuyaGwId + StaticData.NEWLINE +
					                        "productKey : " + tuyaProductKey);
			// ---------------------------------------------------------------------
		} 
    	catch (Exception theException) 
    	{
		}
    	// -------------------------------------------------------------------------
    	// 24/04/2019 ECU changed to use a generalised hex dump via the new method
    	// -------------------------------------------------------------------------
    	return Utilities.hexDump (theBytes);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    
    // =============================================================================
    // 10/05/2019 ECU create a class which contains details of discovered Kasa
    //                devices and which can determine when an announcement has to be
    //                made when the 'relay state' changes
    // -----------------------------------------------------------------------------
    class KasaDevice
    {
    	// -------------------------------------------------------------------------
    	String	alias;
    	int 	relayState;
    	// -------------------------------------------------------------------------
    	
    	// -------------------------------------------------------------------------
    	public KasaDevice (String theJSONData)
    	{
    		try
    		{
    			// -----------------------------------------------------------------
    			// 10/05/2019 ECU get the object which contains the relay state
    			// -----------------------------------------------------------------
    			JSONObject jsonObject = (new JSONObject (theJSONData)).getJSONObject ("system").getJSONObject ("get_sysinfo");
    			// -----------------------------------------------------------------
    			// 30/04/2019 ECU now return the retrieved state
    			// -----------------------------------------------------------------
    			alias 		= jsonObject.getString ("alias");
    			relayState 	= jsonObject.getInt    ("relay_state");
    			// -----------------------------------------------------------------
    			boolean speakAMessage = false;
    			// -----------------------------------------------------------------
    			// 10/05/2019 ECU now check if an entry already exists
    			// -----------------------------------------------------------------
    			if (kasaDevices.size() > 0)
    			{
    				KasaDevice localKasaDevice;
    				for (int index = 0; index < kasaDevices.size (); index++)
    				{
    					localKasaDevice = kasaDevices.get (index);
    					// ---------------------------------------------------------
    					// 10/05/2019 ECU check if the device is already registered
    					// ---------------------------------------------------------
    					if (localKasaDevice.alias.equalsIgnoreCase (alias))
    					{
    						// -----------------------------------------------------
    						// 10/05/2019 ECU there is an entry so check if the relay
    						//                state has changed
    						// -----------------------------------------------------
    						if (localKasaDevice.relayState != relayState)
    						{
    							// -------------------------------------------------
    							// 10/05/2019 ECU the relay state has changed
    							// -------------------------------------------------
    							localKasaDevice.relayState = relayState;
    							// -------------------------------------------------
    							// 10/05/2019 ECU update the record
    							// -------------------------------------------------
    							kasaDevices.set (index,localKasaDevice);
    							// -------------------------------------------------
    							// 10/05/2019 ECU indicate that 'change' message is
    							//                to be spoken
    							// -------------------------------------------------
    							speakAMessage = true;
    							// -------------------------------------------------
    						}
    						// -----------------------------------------------------
    						// 10/05/2019 ECU break out of the 'for' loop
    						// -----------------------------------------------------
    						break;
    						// -----------------------------------------------------
    					}
    				}
    			}
    			else
    			{
    				// -------------------------------------------------------------
    				// 10/05/2019 ECU there are no entries so add this one
    				// -------------------------------------------------------------
    				kasaDevices.add (this);
    				// -------------------------------------------------------------
					// 10/05/2019 ECU indicate that 'change' message is
					//                to be spoken
					// -------------------------------------------------------------
					speakAMessage = true;
					// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			// 10/05/2019 ECU check if a message is to be spoken
    			// -----------------------------------------------------------------
    			if (speakAMessage)
    			{
    				// -------------------------------------------------------------
					// 10/05/2019 ECU tell the user what is happening
					// -------------------------------------------------------------
					Utilities.SpeakAPhrase (MainActivity.activity,alias + 
												" is " + 
													((relayState == RELAY_STATE_ON) ? MainActivity.activity.getString (R.string.on)
																			        : MainActivity.activity.getString (R.string.off)));
					// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    		}
    		catch (Exception theException)
    		{
    			// -----------------------------------------------------------------
    			// 10/05/2019 ECU an exception occurred so report the fact
    			// -----------------------------------------------------------------
    
    			// -----------------------------------------------------------------
    		}
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}

package com.usher.diboson;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

// =================================================================================
public class Cryptography 
{
	// =============================================================================
	
	// =============================================================================
	private SecretKeySpec secretKey;
	// =============================================================================
	
	// =============================================================================
	public Cryptography (String theKey)
	{
		// -------------------------------------------------------------------------
		MessageDigest messageDigest = null;
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 08/05/2019 ECU convert the incoming string key into bytes
			// ---------------------------------------------------------------------
			byte [] localKey = theKey.getBytes ("UTF-8");
			
			messageDigest = MessageDigest.getInstance ("SHA-1");
			// ---------------------------------------------------------------------
			// 08/05/2019 ECU complete the hash computation
			// ---------------------------------------------------------------------
			localKey = messageDigest.digest (localKey);
			// ---------------------------------------------------------------------
			localKey = Arrays.copyOf (localKey,16); 
			// ---------------------------------------------------------------------
			// 08/05/2019 ECU generate the secret key
			// ---------------------------------------------------------------------
			secretKey = new SecretKeySpec (localKey, "AES");
			// ---------------------------------------------------------------------
		} 
		catch (NoSuchAlgorithmException theException) 
		{
		} 
		catch (UnsupportedEncodingException theException) 
		{
	    }
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String decrypt (String theStringToDecrypt) 
	{
		// -------------------------------------------------------------------------
		// 08/05/2019 ECU created to decrypt the string using the generated key
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			Cipher localCipher = Cipher.getInstance ("AES/ECB/PKCS5Padding");
			localCipher.init (Cipher.DECRYPT_MODE,secretKey);
			// ---------------------------------------------------------------------
			// 08/05/2019 ECU return the decrypted string
			// ---------------------------------------------------------------------
			return new String (localCipher.doFinal (Base64.decode (theStringToDecrypt,Base64.DEFAULT)));
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{
		}
		// -------------------------------------------------------------------------
		// 08/05/2019 ECU exception occurred
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String encrypt (String theStringToEncrypt) 
	{
		// -------------------------------------------------------------------------
		// 08/05/2019 ECU created to encrypt the string using the generated key
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			Cipher localCipher = Cipher.getInstance ("AES/ECB/PKCS5Padding");
			localCipher.init (Cipher.ENCRYPT_MODE,secretKey);
			// ---------------------------------------------------------------------
			// 08/05/2019 ECU return the encrypted string
			// ---------------------------------------------------------------------
			return Base64.encodeToString (localCipher.doFinal (theStringToEncrypt.getBytes ("UTF-8")),Base64.DEFAULT);
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{
	    }
		// -------------------------------------------------------------------------
	    return null;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

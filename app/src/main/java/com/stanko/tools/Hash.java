package com.stanko.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;

/**
 * class is used to retrieve MD5 hashes.
 * @author Stan Koshutsky
 *
 */

public class Hash {
	
	private final static String DEBUG_TAG = "MD5";
	
	public static String getMD5(final byte[] bytes) {
		if (bytes==null)
			return null;
	    try {
	        // Create MD5 Hash
	        final MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(bytes);
	        final byte[] md5sum = digest.digest();
	        final BigInteger bigInt = new BigInteger(1, md5sum);
	        final String stringMD5 = bigInt.toString(16);
            // Fill to 32 chars
            return String.format("%32s", stringMD5).replace(' ', '0'); 
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public static String getMD5(String s) {
		if (s==null)
			return null;
		return getMD5(s.getBytes());
	}
	
    public static boolean checkMD5(String md5, File file) {
        if (md5 == null || md5.equals("") || file == null || !file.exists()) {
            Log.e(DEBUG_TAG, "MD5 String NULL or File NULL or File doesn't exists");
            return false;
        }

        String calculatedDigest = getMD5(file);
        if (calculatedDigest == null) {
            Log.e(DEBUG_TAG, "calculatedDigest NULL");
            return false;
        }

        Log.i(DEBUG_TAG, "Calculated digest: " + calculatedDigest);
        Log.i(DEBUG_TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String getMD5(final File file) {
    	if (!FileUtils.isReadable(file)) 
    		return null;
    	
        InputStream inputStream = null;
        try {
        	inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(DEBUG_TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        final String stringMD5 = getMD5(inputStream);
        
        try {
        	inputStream.close();
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Exception on closing MD5 input stream", e);
        }
        
        return stringMD5;

    }
    
    public static String getMD5(InputStream inputStream) {
    	if (inputStream == null)
    		return null;
    	
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(DEBUG_TAG, "Exception while getting Digest", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read=0;
        String stringMD5 = null;
        try {
            while ((read = inputStream.read(buffer)) > 0) 
                digest.update(buffer, 0, read);
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            stringMD5 = bigInt.toString(16);
            // Fill to 32 chars
            stringMD5 = String.format("%32s", stringMD5).replace(' ', '0');
        } catch (IOException e) {
        	Log.e(DEBUG_TAG, "Exception on closing MD5 input stream", e);
            //throw new RuntimeException("Unable to process file for MD5", e);
        	return null;
        } 
//        finally {
//            try {
//            	inputStream.close();
//            } catch (IOException e) {
//                Log.e(DEBUG_TAG, "Exception on closing MD5 input stream", e);
//            }
//        }
        return stringMD5;

    }
    
	public static String getAppKeyHash(Context context, String packageName) {
		String keyHash = null;
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
				android.util.Log.i("APP KEY HASH:", keyHash);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return keyHash;
	}
}

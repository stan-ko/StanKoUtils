package com.stanko.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * class is used to retrieve MD5 and SHA hashes.
 *
 * @author Stan Koshutsky
 */

public class Hash {

    private final static MessageDigest sMD5digest;
    private final static MessageDigest sSHAdigest;

    static {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sMD5digest = digest;
        digest = null;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sSHAdigest = digest;
    }

    /**
     * Calculates MD5 hash of a given byte array.
     *
     * @param bytes
     * @return
     */
    public static String getMD5(final byte[] bytes) {
        if (bytes == null)
            return null;

        if (sMD5digest == null) {
            new NoSuchAlgorithmException().printStackTrace();
            return null;
        } else {
            sMD5digest.reset();
        }

        // calc MD5 Hash
        sMD5digest.update(bytes);
        final byte[] md5sum = sMD5digest.digest();
        final BigInteger bigInt = new BigInteger(1, md5sum);
        final String hash = bigInt.toString(16);
        // Fill to 32 chars
        return String.format("%32s", hash).replace(' ', '0');
    }

    /**
     * Calculates MD5 hash of a given String. Aware of passing big strings to avoid OOM.
     *
     * @param s
     * @return
     */
    public static String getMD5(final String s) {
        if (s == null)
            return null;
        return getMD5(s.getBytes());
    }

    /**
     * Checks if given hash is the same for given file - helps to detect file changes
     *
     * @param md5Hash
     * @param file
     * @return true if MD5 hash for given file is the same to given hash or false otherwise
     */
    public static boolean checkMD5(final String md5Hash, final File file) {
        if (TextUtils.isEmpty(md5Hash) || file == null || !FileUtils.isReadable(file)) {
            Log.e("Given String is empty or File is NULL or File is not readable");
            return false;
        }

        final String hash = getMD5(file);
        if (hash == null) {
            Log.e("calculated hash is NULL");
            return false;
        }

        return hash.equalsIgnoreCase(md5Hash);
    }

    /**
     * Calculates MD5 hash of a given File
     *
     * @param file
     * @return
     */
    public static String getMD5(final File file) {
        if (!FileUtils.isReadable(file))
            return null;

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("Exception while getting FileInputStream", e);
            return null;
        }

        final String hash = getMD5(inputStream);

        try {
            inputStream.close();
        } catch (IOException e) {
            Log.e("Exception on closing input stream", e);
        }

        return hash;

    }

    /**
     * Calculates MD5 hash for a given stream. Warning - pointer will be moved to the end of stream
     * so you have to reset stream or whatever you need to do after running this method.
     * Given stream will not be reset moving its pointer to a begin before calculation so you could
     * calc hash of a part of a stream if you want, just pass stream pointed to a desired position
     * before calling this method.
     * Given stream will not be closed after calculations.
     *
     * @param inputStream
     * @return MD5 hash String
     */
    public static String getMD5(final InputStream inputStream) {
        if (inputStream == null)
            return null;

        if (sMD5digest == null) {
            new NoSuchAlgorithmException().printStackTrace();
            return null;
        } else {
            sMD5digest.reset();
        }

        final byte[] buffer = new byte[8192];
        int read;
        String hash = null;
        try {
            while ((read = inputStream.read(buffer)) > 0)
                sMD5digest.update(buffer, 0, read);
            final byte[] md5sum = sMD5digest.digest();
            final BigInteger bigInt = new BigInteger(1, md5sum);
            hash = bigInt.toString(16);
            // Fill to 32 chars
            hash = String.format("%32s", hash).replace(' ', '0');
        } catch (IOException e) {
            Log.e("Exception on closing MD5 input stream", e);
        }
        return hash;
    }

    /**
     * Calculates MD5 hash for a given stream. Warning: stream will be reset after hash calc.
     * Given stream will not be reset moving its pointer to a begin before calculation so you could
     * calc hash of a part of a stream if you want, just pass stream pointed to a desired position
     * before calling this method.
     * Given stream will not be closed after calculations.
     *
     * @param inputStream
     * @return MD5 hash String
     */
    public static String getMD5AndResetStream(final InputStream inputStream) {
        if (inputStream == null)
            return null;

        final String hash = getMD5(inputStream);
        try {
            inputStream.reset();
        } catch (IOException e) {
            Log.e("Exception on resetting input stream", e);
        }
        return hash;
    }

    /**
     * Calculates SHA hash of a given byte array.
     *
     * @param bytes
     * @return
     */
    public static String getSHA(final byte[] bytes) {
        if (bytes == null)
            return null;

        if (sSHAdigest == null) {
            new NoSuchAlgorithmException().printStackTrace();
            return null;
        } else {
            sSHAdigest.reset();
        }
        // Create SHA Hash
        sSHAdigest.update(bytes);
        return Base64.encodeToString(sSHAdigest.digest(), Base64.DEFAULT);
    }

    /**
     * Calculates SHA hash of a given String. Aware of passing big strings to avoid OOM.
     *
     * @param s
     * @return
     */
    public static String getSHA(final String s) {
        if (s == null)
            return null;
        return getSHA(s.getBytes());
    }

    /**
     * Checks if given hash is the same for given file - helps to detect file changes
     *
     * @param shaHash
     * @param file
     * @return true if SHA hash for given file is the same to given hash or false otherwise
     */
    public static boolean checkSHA(final String shaHash, final File file) {
        if (TextUtils.isEmpty(shaHash) || file == null || !FileUtils.isReadable(file)) {
            Log.e("Given String is NULL or File is NULL or File is not readable");
            return false;
        }

        final String hash = getSHA(file);
        if (hash == null) {
            Log.e("calculated hash is NULL");
            return false;
        }

        return hash.equalsIgnoreCase(shaHash);
    }

    /**
     * Calculates SHA hash of a given File
     *
     * @param file
     * @return
     */
    public static String getSHA(final File file) {
        if (!FileUtils.isReadable(file))
            return null;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("Exception while getting FileInputStream", e);
            return null;
        }

        final String hash = getSHA(inputStream);

        try {
            inputStream.close();
        } catch (IOException e) {
            Log.e("Exception on closing input stream", e);
        }

        return hash;

    }

    /**
     * Calculates SHA hash for a given stream. Warning - pointer will be moved to the end of stream
     * so you have to reset stream or whatever you need to do after running this method.
     * Given stream will not be reset moving its pointer to a begin before calculation so you could
     * calc hash of a part of a stream if you want, just pass stream pointed to a desired position
     * before calling this method.
     * Given stream will not be closed after calculations.
     *
     * @param inputStream
     * @return SHA hash String
     */
    public static String getSHA(final InputStream inputStream) {
        if (inputStream == null)
            return null;

        if (sSHAdigest == null) {
            new NoSuchAlgorithmException().printStackTrace();
            return null;
        } else {
            sSHAdigest.reset();
        }

        final byte[] buffer = new byte[8192];
        int read;
        String hash = null;
        try {
            while ((read = inputStream.read(buffer)) > 0)
                sSHAdigest.update(buffer, 0, read);
            hash = Base64.encodeToString(sSHAdigest.digest(), Base64.DEFAULT);
        } catch (IOException e) {
            Log.e("Exception on closing MD5 input stream", e);
        }
        return hash;
    }

    /**
     * Calculates SHA hash for a given stream. Warning: stream will be reset after hash calc.
     * Given stream will not be reset moving its pointer to a begin before calculation so you could
     * calc hash of a part of a stream if you want, just pass stream pointed to a desired position
     * before calling this method.
     * Given stream will not be closed after calculations.
     *
     * @param inputStream
     * @return SHA hash String
     */
    public static String getSHAAndResetStream(final InputStream inputStream) {
        if (inputStream == null)
            return null;

        final String hash = getSHA(inputStream);
        try {
            inputStream.reset();
        } catch (IOException e) {
            Log.e("Exception on resetting input stream", e);
        }
        return hash;
    }

    /**
     * Returns application SHA hash - the cert fingerprint used to sign the app
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getAppKeyHash(final Context context, final String packageName) {
        if (context==null || TextUtils.isEmpty(packageName))
            return null;
        if (sSHAdigest == null) {
            new NoSuchAlgorithmException().printStackTrace();
            return null;
        } else {
            sSHAdigest.reset();
        }
        String keyHash = null;
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                sSHAdigest.update(signature.toByteArray());
                keyHash = Base64.encodeToString(sSHAdigest.digest(), Base64.DEFAULT);
                Log.i("Application key SHA hash: " + keyHash);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return keyHash;
    }
}

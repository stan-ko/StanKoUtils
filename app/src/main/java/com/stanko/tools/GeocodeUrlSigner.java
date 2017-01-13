package com.stanko.tools;

/**
 * Created by stan on 15.04.16.
 */
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by stan on 05.02.16.
 */
public class GeocodeUrlSigner {

    // The URL shown in these examples is a static URL which should already
    // be URL-encoded. In practice, you will likely have code
    // which assembles your URL from user or web service input
    // and plugs those values into its parameters.
    //private static String urlString = "YOUR_URL_TO_SIGN";

    // This variable stores the binary key, which is computed from the string (Base64) key
    private static byte[] key;

    public static String signUrl(final String client, final String key, final String inputUrl) {

        // Convert the string to a URL so we can parse it
        final URL url;
        String result = null;
        try {
            url = new URL(inputUrl+"&client="+client);
            final GeocodeUrlSigner signer = new GeocodeUrlSigner(key);
            final String request = signer.signRequest(url.getPath(),url.getQuery());
            result = url.getProtocol() + "://" + url.getHost() + request;
//            Log.i("Signed URL :" + url.getProtocol() + "://" + url.getHost() + request);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private GeocodeUrlSigner(String keyString) throws IOException {
        // Convert the key from 'web safe' base 64 to binary
        keyString = keyString.replace('-', '+');
        keyString = keyString.replace('_', '/');
        System.out.println("Key: " + keyString);
        // Base64 is JDK 1.8 only - older versions may need to use Apache Commons or similar.
        key = Base64.decode(keyString,Base64.DEFAULT);
    }

    private String signRequest(String path, String query) throws NoSuchAlgorithmException,
            InvalidKeyException, UnsupportedEncodingException, URISyntaxException {

        // Retrieve the proper URL components to sign
        String resource = path + '?' + query;

        // Get an HMAC-SHA1 signing key from the raw key bytes
        SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1");

        // Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1 key
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(sha1Key);

        // compute the binary signature for the request
        byte[] sigBytes = mac.doFinal(resource.getBytes());

        // base 64 encode the binary signature
        // Base64 is JDK 1.8 only - older versions may need to use Apache Commons or similar.
        String signature = Base64.encodeToString(sigBytes,Base64.DEFAULT);

        // convert the signature to 'web safe' base 64
        signature = signature.replace('+', '-');
        signature = signature.replace('/', '_');

        return resource + "&signature=" + signature;
    }
}
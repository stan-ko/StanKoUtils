package com.stanko.tools;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * AsyncTask based class for checking
 * Internet availability and server reachabilty
 *
 * @author Stan Koshutsky
 */
public class InternetConnectionHelper extends AsyncTask<Void, Void, Integer> {

    public static final int TIME_OUT = 1000 * 3; //3s

    public static final int SC_OK = 0;
    public static final int SC_NO_INTERNET_CONNECTION_AVAILABLE = 1;
    public static final int SC_SERVER_COULD_NOT_BE_REACHED = 2;

    public static final int SC_INTERNET_OVER_WIFI = 16;
    public static final int SC_INTERNET_OVER_MOBILE = 32;
    public static final int SC_INTERNET_OVER_OTHER = 64;

    private static boolean isInternetWiFi;
    private static boolean isInternetWiMax;
    private static boolean isInternetMobile;
    private static boolean isInternetOther;

    private static int connectionType;
    public final long startedMillis;
    private final Context appContext;
    private final String hostUrl;
    private WeakReference<ServerChecking> activity;
    private int result;

    /**
     * constructor accepts Activity which implements ServerChecking
     *
     * @param activity
     * @param hostUrl
     * @param <T>
     */
    public <T extends Context & ServerChecking> InternetConnectionHelper(final T activity, final String hostUrl) {
        super();
        this.activity = new WeakReference<ServerChecking>(activity);
        this.appContext = activity.getApplicationContext();
        this.hostUrl = hostUrl;
        startedMillis = System.currentTimeMillis();
    }

    /**
     * constructor accepts any class which implements ServerChecking but needs Application Context
     *
     * @param activity
     * @param context
     * @param hostUrl
     * @param <T>
     */
    public <T extends ServerChecking> InternetConnectionHelper(final T activity, final Context context, final String hostUrl) {
        super();
        this.activity = new WeakReference<ServerChecking>(activity);
        this.appContext = context.getApplicationContext();
        this.hostUrl = hostUrl;
        startedMillis = System.currentTimeMillis();
    }

    public static boolean isInternetMobile() {
        return isInternetMobile;
    }

    public static boolean checkIsNetworkAvailable(final Context context) {
        return isNetworkConnectionAvailable(context);
    }

    public static boolean isNetworkConnectionAvailable(final Context context) {
        if (context == null)
            return false;

        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
        } // like method not found?

        if (cm == null)
            return false;

        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getActiveNetworkInfo();
        } catch (Exception e) {
        } // like method not found?

        isInternetOther = networkInfo != null && networkInfo.isConnected();

        return isInternetOther;
    }

    /**
     * method checks if any ({@link #SC_INTERNET_OVER_WIFI},{@link #SC_INTERNET_OVER_MOBILE},
     * {@link #SC_INTERNET_OVER_OTHER}) network connection is available.
     * 1) issue1: assumes the connection is an Internet connection
     * 2) issue2: fails if a connection requires a login/password (like hotels WiFi)
     *
     * @return
     */
    public static boolean isNetworkConnectionAvailableExt(final Context context) {

        isInternetWiFi = false;
        isInternetMobile = false;
        isInternetWiMax = false;
        isInternetOther = false;
        connectionType = 0;

        if (context == null)
            return false;

        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
        }// like method not found?

        if (cm == null)
            return false;

        // determine the type of network
//		int TYPE_BLUETOOTH		The Bluetooth data connection.
//		int TYPE_DUMMY			Dummy data connection.
//		int TYPE_ETHERNET		The Ethernet data connection.
//		int TYPE_MOBILE			The Mobile data connection.
//		int TYPE_MOBILE_DUN		A DUN-specific Mobile data connection.
//		int TYPE_MOBILE_HIPRI	A High Priority Mobile data connection.
//		int TYPE_MOBILE_MMS		An MMS-specific Mobile data connection.
//		int TYPE_MOBILE_SUPL	A SUPL-specific Mobile data connection.
//		int TYPE_WIFI			The WIFI data connection.
//		int TYPE_WIMAX			The WiMAX data connection.

        final NetworkInfo niMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        final NetworkInfo niWiFi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo niWiMax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        final NetworkInfo niOther = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (niOther != null)
            connectionType = niOther.getType();

        isInternetWiFi = niWiFi != null && niWiFi.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetMobile = niMobile != null && niMobile.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetWiMax = niWiMax != null && niWiMax.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetOther = niOther != null && niOther.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;

        return (isInternetWiFi || isInternetWiMax || isInternetMobile || isInternetOther);
    }

    /**
     * method checks if an Internet connection is available via WiFi
     * 1) issue1: assumes the WiFi connection is an Internet connection
     * 2) issue2: fails if a WiFi connection requires a login/password
     *
     * @return
     */
    public static boolean isNetworkAvailableViaWiFi(final Context context) {

        isInternetWiFi = false;
        isInternetMobile = false;
        isInternetWiMax = false;
        isInternetOther = false;
        connectionType = 0;

        if (context == null)
            return false;

        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
        }// like method not found?

        if (cm == null)
            return false;

        // determine the type of network
//		int TYPE_BLUETOOTH		The Bluetooth data connection.
//		int TYPE_DUMMY			Dummy data connection.
//		int TYPE_ETHERNET		The Ethernet data connection.
//		int TYPE_MOBILE			The Mobile data connection.
//		int TYPE_MOBILE_DUN		A DUN-specific Mobile data connection.
//		int TYPE_MOBILE_HIPRI	A High Priority Mobile data connection.
//		int TYPE_MOBILE_MMS		An MMS-specific Mobile data connection.
//		int TYPE_MOBILE_SUPL	A SUPL-specific Mobile data connection.
//		int TYPE_WIFI			The WIFI data connection.
//		int TYPE_WIMAX			The WiMAX data connection.

        final NetworkInfo niMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        final NetworkInfo niWiFi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo niWiMax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        final NetworkInfo niOther = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (niOther != null)
            connectionType = niOther.getType();

        isInternetWiFi = niWiFi != null && niWiFi.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetMobile = niMobile != null && niMobile.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetWiMax = niWiMax != null && niWiMax.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetOther = niOther != null && niOther.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;

        return (isInternetWiFi || isInternetWiMax);
    }

    /**
     * Method uses deprecated ConnectivityManager.requestRouteToHost() method to ensure the host
     * is available.
     *
     * @param context  - Application Context
     * @param hostName - the host to check
     * @return true if host available
     */
    public static boolean isHostReachable(final Context context, final String hostName) {

        // check if there is http:// or like at the start of hostName
        final String pureHostName;
        if (hostName.contains("://"))
            pureHostName = hostName.substring(hostName.indexOf("://") + 3).trim(); // cut off http://, https://, ftp:// etc
        else
            pureHostName = hostName.trim();

        final int serverIP = getHostIntIP(pureHostName);
        //Log.i("ServerChecker", "getHostIntIP("+host+"): "+serverIP);
        if (serverIP != 0) {
            // check serverIP is reachable
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (isInternetWiFi)
                isInternetWiFi = cm.requestRouteToHost(ConnectivityManager.TYPE_WIFI, serverIP);

            else if (isInternetMobile)
                isInternetMobile = cm.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, serverIP);

            else if (isInternetWiMax)
                isInternetWiMax = cm.requestRouteToHost(ConnectivityManager.TYPE_WIMAX, serverIP);

            else if (isInternetOther) {
                connectionType = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType();
                isInternetOther = cm.requestRouteToHost(connectionType, serverIP);
            }
            return (isInternetWiFi || isInternetWiMax || isInternetMobile || isInternetOther);
        } else
            return isHostReachable(hostName);
    }

    /**
     * Obtains InetAddress (Android IP representation) of a given host
     *
     * @param hostName - the host
     * @return InetAddress of a given host
     */
    public static InetAddress getHostIP(final String hostName) {
        // check if there is http:// at the start of hostName
        final String host;
        if (hostName.contains("://"))
            host = hostName.substring(hostName.indexOf("://") + 3);
        else
            host = hostName;

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            Log.e(e);
        }
        return inetAddress;
    }

    /**
     * Calculates integer representation of an IP
     *
     * @param hostname - host to get its IP
     * @return int IP representation
     */
    public static int getHostIntIP(String hostname) {
        final InetAddress inetAddress = getHostIP(hostname);
        if (inetAddress == null)
            return 0;

        final byte[] addressBytes = inetAddress.getAddress();
        final int address = (addressBytes[3] & 0xff) << 24
                | (addressBytes[2] & 0xff) << 16
                | (addressBytes[1] & 0xff) << 8
                | addressBytes[0] & 0xff;
        return address;
    }

    /**
     * Checks host by connection to using HttpURLConnection. Method should not be run
     * on a Main/UIThread otherwise exception will be thrown.
     *
     * @param hostUrl - the host to check. By default http:// prefix will be added if no any
     * @return true if host reachable (connection were established)
     */
    public static boolean isHostReachable(String hostUrl) {

        // adding http:// if its just a pure host name like google.com instead of http://google.com
        if (!hostUrl.contains("://"))
            hostUrl = "http://" + hostUrl;

        boolean isReachable = false;
        boolean isExceptionHappens = false;
        try {
            final URL url = new URL(hostUrl);
            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "Android Application");
            httpURLConnection.setRequestProperty("Connection", "close");
            httpURLConnection.setConnectTimeout(TIME_OUT); // Timeout in seconds
            httpURLConnection.connect();
            final int responseCode = httpURLConnection.getResponseCode();
            isReachable = responseCode == HttpURLConnection.HTTP_OK //200
                            || responseCode == HttpURLConnection.HTTP_BAD_METHOD
                            || responseCode == HttpURLConnection.HTTP_BAD_REQUEST
                            || responseCode == HttpURLConnection.HTTP_ACCEPTED
                            || responseCode == HttpURLConnection.HTTP_FORBIDDEN
                            || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED;

        } catch (MalformedURLException e) {
            isExceptionHappens = true;
            Log.e("ServerChecker", e);
        } catch (IOException e) {
            isExceptionHappens = true;
            Log.e("ServerChecker", e);
        }

//        // maybe host is not reachable using GET method? Lets try with POST
//        if (!isReachable && !isExceptionHappens){
//            try {
//                final URL url = new URL(hostUrl);
//                final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//                httpURLConnection.setRequestProperty("User-Agent", "Android Application");
//                httpURLConnection.setRequestProperty("Connection", "close");
//                httpURLConnection.setConnectTimeout(TIME_OUT); // Timeout in seconds
//                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.connect();
//                isReachable = httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK; //200
//            } catch (MalformedURLException e) {
//                Log.e("ServerChecker", e);
//            } catch (IOException e) {
//                Log.e("ServerChecker",e);
//            }
//        }

        return isReachable;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (hostUrl == null || hostUrl.length() == 0)
            return null;

        return performCheck();
    }

    @Override
    protected void onPostExecute(final Integer result) {
        this.result = result;
        postResult();
    }

    public void postResult() {
        ServerChecking caller = activity.get();
        if (caller != null) {
            // if caller is an Activity and its finished already
            // we have no caller to return result to
            if (caller instanceof Activity && ((Activity) caller).isFinishing() || isCancelled())
                return;
            caller.handleServerCheckingResult(result);
        }
        activity.clear();
    }

    /**
     * to detach this from caller
     */
    public void detach() {
        activity.clear();// = null;
    }

    public <T extends Activity & ServerChecking> void attach(final T activity) {
        this.activity = new WeakReference<ServerChecking>(activity);
        if (getStatus() == AsyncTask.Status.FINISHED)
            postResult();
    }

    /**
     * method provides check if an Internet connection is available
     */
    private int performCheck() {
        //ServerChecking caller = activity.get();
        if (!checkIsNetworkAvailable(appContext))
            return SC_NO_INTERNET_CONNECTION_AVAILABLE;
        else if (!isHostReachable(appContext, hostUrl))
            return SC_SERVER_COULD_NOT_BE_REACHED;
        else
            return SC_OK
                    + (isInternetWiFi || isInternetWiMax ? SC_INTERNET_OVER_WIFI : 0)
                    + (isInternetMobile ? SC_INTERNET_OVER_MOBILE : 0)
                    + (isInternetOther ? SC_INTERNET_OVER_OTHER : 0);
    }

    // interface (callback) to deliver host availability check result
    public interface ServerChecking {
        public void handleServerCheckingResult(final int result);
    }
}
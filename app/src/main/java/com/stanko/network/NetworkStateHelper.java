package com.stanko.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.text.TextUtils;

import com.stanko.tools.BooleanLock;
import com.stanko.tools.Log;
import com.stanko.tools.StoppableThread;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Stan Koshutsky <stan.koshutsky@gmail.com> on 03.09.2015.
 */
public class NetworkStateHelper {

    public static final int TIME_OUT = 1000 * 3; //3s

    final static BooleanLock checkIfHostRespondsLock = new BooleanLock();
    private static Context sAppContext;
    private static NetworkState sLastNetworkState;
    private static String sHostToCheck;
    private static boolean isNetworkAvailable;
    private static boolean isHostReachable;
    private static StoppableThread sCheckIfHostRespondsThread;

    public static void init(final Context context) {
        init(context, null);
    }

    public static void init(final Context context, final String hostToCheck) {
        if (sAppContext == null)
            sAppContext = context.getApplicationContext();
        isNetworkAvailable = isNetworkConnectionAvailable(sAppContext);
        if (!TextUtils.isEmpty(hostToCheck)) {
            sHostToCheck = hostToCheck;
            if (isNetworkAvailable) {
                checkIfHostResponds(null, NetworkState.NRGotNetwork, null, null);
            }
        }
    }

    /**
     * returns NetworkStateReceiver
     *
     * @param context - Application Context
     * @return NetworkStateReceiver
     */
    public static NetworkStateReceiver getReceiver(final Context context) {
        init(context);
        return new NetworkStateReceiver(sAppContext);
    }

    /**
     * returns NetworkStateReceiver
     *
     * @param context - Application Context
     * @param host    - the host to check availability of
     * @return NetworkStateReceiver
     */
    public static NetworkStateReceiver getReceiver(final Context context, final String host) {
        init(context);
        sHostToCheck = host;
        return new NetworkStateReceiver(sAppContext);
    }

    public static void setHostToCheck(final String hostToCheck) {
        NetworkStateHelper.sHostToCheck = hostToCheck;
    }

    /**
     * Returns android.net.ConnectivityManager.CONNECTIVITY_ACTION ("android.net.conn.CONNECTIVITY_CHANGE")
     * as a IntentFilter action
     *
     * @return IntentFilter
     */
    public static IntentFilter getReceiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION); // "android.net.conn.CONNECTIVITY_CHANGE"
        return intentFilter;
    }

    /**
     * Checks if currently device has any network connection without ensuring it has Internet access.
     *
     * @return true if connection persists or false otherwise
     */
    public static boolean isNetworkAvailable() {
        Log.i("isNetworkAvailable(): isNetworkAvailable: " + isNetworkAvailable);
        if (!isNetworkAvailable) {
            final boolean isNetworkConnectionAvailable = isNetworkConnectionAvailable(sAppContext);
            Log.i("isNetworkConnectionAvailable(): " + isNetworkConnectionAvailable);
            // if network connection available but it wasn't
            if (isNetworkConnectionAvailable)
                checkIfHostResponds(sLastNetworkState, NetworkState.NRGotNetwork, null, null);
            return isNetworkConnectionAvailable;
        } else {
            return isNetworkAvailable && isHostReachable;
        }
    }

    /**
     * Called from NetworkStateReceiver
     *
     * @param wasNetworkAvailable
     * @param isNetworkAvailable
     * @param lastNetworkState
     * @param newNetworkState
     * @param newNetworkID
     * @param lastNetworkID
     */
    static void handleNetworkState(final boolean wasNetworkAvailable,
                                   final boolean isNetworkAvailable,
                                   final NetworkState lastNetworkState,
                                   final NetworkState newNetworkState,
                                   final String newNetworkID,
                                   final String lastNetworkID) {
        if (TextUtils.equals(lastNetworkID, newNetworkID) && lastNetworkState == newNetworkState && wasNetworkAvailable == isNetworkAvailable) {
            Log.i(NetworkStateHelper.class, "handleNetworkState(): same state -> ignoring");
            return;
        }
        // check if last lastNetworkID is WiFi's one (SSID/BSSID) and is it the same
        // or we switched from one WiFi network to another (networkId is changed)
        // cuz if its WiFi but networkId is changed - it is another WiFi network
        // and it could be a LAN via WiFi (with no Internet connection available)
        // boolean isWiFiNetworkChanged = !newNetworkID.equals(lastNetworkID);

        NetworkStateHelper.isNetworkAvailable = isNetworkAvailable;
        NetworkStateHelper.sLastNetworkState = newNetworkState;
        if (isNetworkAvailable && !TextUtils.isEmpty(sHostToCheck)) {
            checkIfHostResponds(lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
        } else {
            final EventBus eventBus = EventBus.getDefault();
            if (eventBus.hasSubscriberForEvent(NetworkStateReceiverEvent.class)) {
                eventBus.post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, isHostReachable, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
            }
        }
    }

    /**
     * Handles request to Host to check if it responds called by NetworkStateReceiver
     *
     * @param lastNetworkState
     * @param newNetworkState
     * @param lastNetworkID
     * @param newNetworkID
     */
    static void checkIfHostResponds(final NetworkState lastNetworkState,
                                    final NetworkState newNetworkState,
                                    final String lastNetworkID,
                                    final String newNetworkID) {
        if (TextUtils.isEmpty(sHostToCheck)) {
            Log.e(new Exception("NetworkStateHelper: Can't start checkIfHostResponds() task - sHostToCheck is empty"));
        }

        synchronized (checkIfHostRespondsLock) {
            // cancel current Host responding check thread if any
            if (checkIfHostRespondsLock.isRunning() && sCheckIfHostRespondsThread != null) {
                sCheckIfHostRespondsThread.isStopped = true;
                checkIfHostRespondsLock.setFinished();
            }

            if (!checkIfHostRespondsLock.setRunning()) {
                Log.e(new Exception("NetworkStateHelper: Can't start checkIfHostResponds() task - checkIfHostRespondsLock is locked"));
                return;
            }
        }

        // Creating and starting a thread for sending a request to Host
        sCheckIfHostRespondsThread = new StoppableThread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                Looper.prepare();
                final StoppableThread thisThread = ((StoppableThread) Thread.currentThread());
                final boolean doesHostRespond = isHostReachable(sHostToCheck);
                // if thread stop requested task result will be ignored
                if (thisThread.isStopped)
                    return;

                synchronized (checkIfHostRespondsLock) {
                    NetworkStateHelper.isHostReachable = doesHostRespond;
                    final EventBus eventBus = EventBus.getDefault();
                    if (eventBus.hasSubscriberForEvent(NetworkStateReceiverEvent.class)) {
                        eventBus.post(new NetworkStateReceiverEvent(false, isNetworkAvailable, doesHostRespond, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
                    }
                    checkIfHostRespondsLock.setFinished();
                }

                Looper.loop();
            }
        });
        sCheckIfHostRespondsThread.start();
    }

    /**
     * Checks host by connection to using HttpURLConnection. Method should not be run
     * on a Main/UIThread otherwise exception will be thrown.
     *
     * @param hostUrl - the host to check. By default http:// prefix will be added if no any
     * @return true if host reachable (connection were established)
     */
    public static boolean isHostReachable(final String hostUrl) {
        boolean doesHostRespond = false;
        try {
            // adding http:// if its just a pure host name like google.com instead of http://google.com
            final URL url = new URL(hostUrl.contains("://") ? hostUrl : "http://" + hostUrl);
            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "Android Application");
            httpURLConnection.setRequestProperty("Connection", "close");
            httpURLConnection.setConnectTimeout(TIME_OUT); // Timeout in seconds
            httpURLConnection.connect();
            final int responseCode = httpURLConnection.getResponseCode();
            // https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
            // there are too many codes, guess checking if its gt 0 is enough
            doesHostRespond = responseCode > 0;
        } catch (MalformedURLException e) {
//            isExceptionHappens = true;
//            Log.e("ServerChecker", e);
        } catch (IOException e) {
//            isExceptionHappens = true;
//            Log.e("ServerChecker", e);
        } catch (SecurityException e) {
            // user could limit Internet access so app could crash here
            Log.e(e);
        }
        return doesHostRespond;
    }

    /**
     * Checks if given host responds. Uses callback interface ICheckIfHostResponds
     * Could be run in MainUI/MainThread.
     */
    public static void checkIfHostResponds(final String hostUrl, final ICheckIfHostResponds callback) {
        // Creating and starting a thread for sending a request to Host
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                boolean doesHostRespond = isHostReachable(hostUrl);
                // sending check result using callback
                callback.doesHostRespond(doesHostRespond);
                Looper.loop();
            }
        }).start();
    }

    /**
     * Interface to deliver result of {@link #checkIfHostResponds(String, ICheckIfHostResponds)}
     */
    public interface ICheckIfHostResponds {
        void doesHostRespond(boolean doestIt);
    }


    /**
     * Checks if currently device has any network connection without ensuring it has Internet access
     *
     * @return true if connection persists or false otherwise
     */
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

        return networkInfo != null && networkInfo.isConnected();
    }


    /**
     * method checks if any (INTERNET_OVER_WIFI,INTERNET_OVER_MOBILE,
     * INTERNET_OVER_OTHER) network connection is available.
     * 1) issue1: assumes the network connection is an Internet connection
     * 2) issue2: fails if a connection requires a login/password (like hotels WiFi)
     *
     * @return
     */
    public static boolean isNetworkConnectionAvailableExt(final Context context) {

        boolean isInternetWiFi = false;
        boolean isInternetMobile = false;
        boolean isInternetWiMax = false;
        boolean isInternetOther = false;
        int connectionType = 0;

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

        boolean isInternetWiFi = false;
//        boolean isInternetMobile = false;
        boolean isInternetWiMax = false;
//        boolean isInternetOther = false;
//        int connectionType = 0;


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

//        final NetworkInfo niMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        final NetworkInfo niWiFi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo niWiMax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
//        final NetworkInfo niOther = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

//        if (niOther != null)
//            connectionType = niOther.getType();

        isInternetWiFi = niWiFi != null && niWiFi.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetWiMax = niWiMax != null && niWiMax.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
//        isInternetMobile = niMobile != null && niMobile.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
//        isInternetOther = niOther != null && niOther.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;

        return (isInternetWiFi || isInternetWiMax);
    }


}

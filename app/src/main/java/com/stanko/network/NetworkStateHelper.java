package com.stanko.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.text.TextUtils;

import com.stanko.tools.BooleanLock;
import com.stanko.tools.InternetConnectionHelper;
import com.stanko.tools.Log;
import com.stanko.tools.StoppableThread;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Stan Koshutsky <stan.koshutsky@gmail.com> on 03.09.2015.
 */
public class NetworkStateHelper {

    final static BooleanLock checkIfHostRespondsLock = new BooleanLock();
    private static Context sAppContext;
    private static NetworkState sLastNetworkState;
    private static String sHostToCheck;
    private static boolean isNetworkAvailable;
    private static StoppableThread sCheckIfHostRespondsThread;

    public static void init(final Context context) {
        init(context,null);
    }

    public static void init(final Context context, final String hostToCheck) {
        if (sAppContext == null)
            sAppContext = context.getApplicationContext();
        if (!TextUtils.isEmpty(hostToCheck))
            sHostToCheck = hostToCheck;
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
        if (!isNetworkAvailable) {
            final boolean wasNetworkAvailable = isNetworkAvailable;
            final boolean isNetworkConnectionAvailable = InternetConnectionHelper.checkIsNetworkAvailable(sAppContext);
//            Log.i(NetworkStateReceiver.class, "static isInternetAvailable(): false, isNetworkConnectionAvailable: " + isNetworkConnectionAvailable);
            if (isNetworkConnectionAvailable) {
                checkIfHostResponds(wasNetworkAvailable,
                        isNetworkAvailable,
                        sLastNetworkState,
                        NetworkState.NRGotNetwork,
                        null,
                        null);
            }
            return isNetworkConnectionAvailable;
        } else {
            //Log.i(NetworkStateHelper.class, "static isInternetAvailable(): true");
            return true;
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
    static void handleNetworkState(final boolean wasNetworkAvailable, final boolean isNetworkAvailable, final NetworkState lastNetworkState, final NetworkState newNetworkState, String newNetworkID, final String lastNetworkID) {
        if (TextUtils.equals(lastNetworkID, newNetworkID) && lastNetworkState == newNetworkState && wasNetworkAvailable == isNetworkAvailable) {
            Log.i(NetworkStateHelper.class, "handleNetworkState(): same state -> ignoring");
            return;
        }
        // check if last lastNetworkID is WiFi's one (SSID/BSSID) and is it the same
        // or we switched from one WiFi network to another (networkId is changed)
        // cuz if its WiFi but networkId is changed - it is another WiFi network
        // and it could be a LAN via WiFi (with no Internet connection available)
        // boolean isWiFiNetworkChanged = !newNetworkID.equals(lastNetworkID);

        if (isNetworkAvailable && !TextUtils.isEmpty(sHostToCheck)) {
            checkIfHostResponds(wasNetworkAvailable, isNetworkAvailable, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
            return;
        }
        NetworkStateHelper.isNetworkAvailable = isNetworkAvailable;
        NetworkStateHelper.sLastNetworkState = newNetworkState;
        final EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(NetworkStateReceiverEvent.class)) {
            eventBus.post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
        }
    }

    /**
     * Handles request to Host to check if it responds called by NetworkStateReceiver
     *
     * @param wasNetworkAvailable
     * @param isNetworkAvailable
     * @param lastNetworkState
     * @param newNetworkState
     * @param lastNetworkID
     * @param newNetworkID
     */
    static void checkIfHostResponds(final boolean wasNetworkAvailable, final boolean isNetworkAvailable, final NetworkState lastNetworkState, final NetworkState newNetworkState, final String lastNetworkID, final String newNetworkID) {
        synchronized (checkIfHostRespondsLock) {
            // cancel current Host responding check thread if any
            if (checkIfHostRespondsLock.isRunning() && sCheckIfHostRespondsThread != null) {
                sCheckIfHostRespondsThread.isStopped = true;
                checkIfHostRespondsLock.setFinished();
            }

            if (!checkIfHostRespondsLock.setRunning()) {
                Log.e(new Exception("NetworkStateHelper: Can't start thread in checkIfHostResponds() - checkIfHostRespondsLock is locked"));
                return;
            }
        }

        // Creating and starting a thread for sending a request to Host
        if (!TextUtils.isEmpty(sHostToCheck)) {
            sCheckIfHostRespondsThread = new StoppableThread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    Looper.prepare();
                    final StoppableThread thisThread = ((StoppableThread) Thread.currentThread());
                    boolean doesHostRespond = isHostReachable(sHostToCheck);
                    // if thread stop requested task result will be ignored
                    if (!thisThread.isStopped) {
                        synchronized (checkIfHostRespondsLock) {
                            final EventBus eventBus = EventBus.getDefault();
                            if (eventBus.hasSubscriberForEvent(NetworkStateReceiverEvent.class)) {
                                eventBus.post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, doesHostRespond, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
                            }
                            NetworkStateHelper.isNetworkAvailable = isNetworkAvailable;
                            NetworkStateHelper.sLastNetworkState = newNetworkState;
                            checkIfHostRespondsLock.setFinished();
                        }
                    }
                    Looper.loop();
                }
            });
            sCheckIfHostRespondsThread.start();
        }
    }

    /**
     * Checks if Host responds. Method uses networking and thus could not be run on a main thread.
     * Should be run in AsyncTask or any other Thread.
     */
    public static boolean isHostReachable(final String hostUrl) {
        boolean doesHostRespond = false;
        try {
            doesHostRespond = InternetConnectionHelper.isHostReachable(hostUrl);
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
     * Interface to deliver result of {@link #checkIfHostResponds(String,ICheckIfHostResponds)}
     */
    public interface ICheckIfHostResponds {
        void doesHostRespond(boolean doestIt);
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

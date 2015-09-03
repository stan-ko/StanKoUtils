package com.stanko.network;

import android.content.Context;
import android.text.TextUtils;

import com.stanko.tools.BooleanLock;
import com.stanko.tools.InternetConnectionHelper;
import com.stanko.tools.Log;
import com.stanko.tools.StoppableThread;

import de.greenrobot.event.EventBus;

/**
 * Created by Stan Koshutsky <Stan.Koshutsky@gmail.com> on 03.09.2015.
 */
class NetworkStateHelper {

    private static Context appContext;
    private static NetworkState lastNetworkState;
    private static String hostToCheck;
    private static boolean isNetworkAvailable;
    private static StoppableThread checkIfServerRespondsThread;
    final static BooleanLock checkIfServerRespondsLock = new BooleanLock();

    public static void init(final Context context) {
        if (appContext == null)
            appContext = context.getApplicationContext();
    }

    public static NetworkStateReceiver getReceiver(final Context context) {
        init(context);
        return new NetworkStateReceiver(appContext);
    }

    public static NetworkStateReceiver getReceiver(final Context context, final String host) {
        init(context);
        hostToCheck = host;
        return new NetworkStateReceiver(appContext);
    }

    public static boolean isInternetAvailable() {
        if (!isNetworkAvailable) {
            final boolean wasNetworkAvailable = isNetworkAvailable;
            final boolean isNetworkConnectionAvailable = InternetConnectionHelper.checkIsNetworkAvailable(appContext);
//            Log.i(NetworkStateReceiver.class, "static isInternetAvailable(): false, isNetworkConnectionAvailable: " + isNetworkConnectionAvailable);
            if (isNetworkConnectionAvailable) {
//                // send message from static method to instance to call a instance method
//                EventBus.getDefault().post(new NetworkStateReceiver.NetworkCheckStateImplicitEvent(true));
                checkIfServerResponds(wasNetworkAvailable,
                        isNetworkAvailable,
                        lastNetworkState,
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
     * method schedules a timed request to server to check if it responds
     */
    public static void checkIfServerResponds(final boolean wasNetworkAvailable, final boolean isNetworkAvailable, final NetworkState lastNetworkState, final NetworkState newNetworkState, final String lastNetworkID, final String newNetworkID) {
        synchronized (checkIfServerRespondsLock) {
            // cancel current server responding check thread if any
            if (checkIfServerRespondsLock.isRunning() && checkIfServerRespondsThread != null) {
                checkIfServerRespondsThread.isStopped = true;
                checkIfServerRespondsLock.setFinished();
            }

            if (!checkIfServerRespondsLock.setRunning()) {
                Log.e(new Exception("NetworkStateHelper: Can't start thread in checkIfServerResponds() - checkIfServerRespondsLock is locked"));
                return;
            }
        }

        // Creating and starting a thread for sending a request to server
        checkIfServerRespondsThread = new StoppableThread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                final StoppableThread thisThread = ((StoppableThread) Thread.currentThread());
                boolean doesHostRespond = false;
                try {
                    doesHostRespond = InternetConnectionHelper.checkHostByConnection(hostToCheck);
                } catch (SecurityException e) {
                    // user could limit Internet access so app could crash here
                    //Crashlytics.getInstance().core.logException(e);
                    Log.e(e);
                }
                // sending the message for continuing Activity execution
                if (!thisThread.isStopped) {
                    synchronized (checkIfServerRespondsLock) {
                        EventBus.getDefault().post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, doesHostRespond, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
                        NetworkStateHelper.isNetworkAvailable = isNetworkAvailable;
                        NetworkStateHelper.lastNetworkState = newNetworkState;
                        checkIfServerRespondsLock.setFinished();
                    }
                }
            }
        });
        checkIfServerRespondsThread.start();
    }

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

        if (isNetworkAvailable && !TextUtils.isEmpty(hostToCheck)) {
            checkIfServerResponds(wasNetworkAvailable, isNetworkAvailable, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
            return;
        }
        final EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(NetworkStateReceiverEvent.class)) {
            NetworkStateHelper.isNetworkAvailable = isNetworkAvailable;
            eventBus.post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
            NetworkStateHelper.lastNetworkState = newNetworkState;
        }
    }

}

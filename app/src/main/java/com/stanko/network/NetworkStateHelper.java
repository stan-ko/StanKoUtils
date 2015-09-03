package com.stanko.network;

import android.content.Context;
import android.text.TextUtils;

import com.stanko.tools.BooleanLock;
import com.stanko.tools.Log;

import de.greenrobot.event.EventBus;

/**
 * Created by Stan Koshutsky <Stan.Koshutsky@gmail.com> on 03.09.2015.
 */
class NetworkStateHelper {

    private static Context appContext;
    private static NetworkState lastNetworkState;
    private static String hostToCheck;
    private static boolean isNetworkAvailable;


    public static void init(final Context context){
        if (appContext==null)
            appContext = context.getApplicationContext();
    }

    public static NetworkStateReceiver getReceiver(final Context context){
        init(context);
        return new NetworkStateReceiver(appContext);
    }

    public static NetworkStateReceiver getReceiver(final Context context, final String host){
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

    final static BooleanLock checkIfServerRespondsTask = new BooleanLock();
    /**
     * method schedules a timed request to server to check if it responds
     */
    public static void checkIfServerResponds(final boolean wasNetworkAvailable, final boolean isNetworkAvailable, final NetworkState lastNetworkState, final NetworkState newNetworkState, final String lastNetworkID, final String newNetworkID) {
        synchronized (checkIfServerRespondsTask){
            if (!checkIfServerRespondsTask.setRunning())
                return;
        }

        // Creating and starting a thread for sending a request to server
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                boolean doesHostRespond = false;
                try {
                    doesHostRespond = InternetConnectionHelper.checkHostByConnection(hostToCheck);
                } catch (SecurityException e) {
                    // user could limit Internet access so app could crash here
                    //Crashlytics.getInstance().core.logException(e);
                    Log.e(e);
                }
                // sending the message for continuing Activity execution
                EventBus.getDefault().post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, doesHostRespond, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
                NetworkStateHelper.isNetworkAvailable = isNetworkAvailable;
                NetworkStateHelper.lastNetworkState = newNetworkState;
                synchronized (checkIfServerRespondsTask){
                    checkIfServerRespondsTask.setFinished();
                }
            }
        }).start();

    }

//    private void checkIfServerResponds(final boolean wasNetworkAvailable, final boolean isNetworkAvailable, final NetworkState lastNetworkState, final NetworkState newNetworkState, final String lastNetworkID, final String newNetworkID) {
//        Log.i(NetworkStateReceiver.class, "checkIfServerResponds()");
//        // Creating and starting a thread for sending a request to server
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean doesHostRespond = false;
//                try {
//                    doesHostRespond = InternetConnectionHelper.checkHostByConnection(APIHelper.YT_API_HOST);
//                } catch (SecurityException e) {
//                    // user could limit Internet access so app could crash here
//                    Crashlytics.getInstance().core.logException(e);
//                }
//                Log.i(NetworkStateReceiver.class, "checkIfServerResponds() doesIt: " + doesHostRespond);
//
//                // sending the message for continuing Activity execution
//                EventBus.getDefault().post(new NetworkStateReceiverEvent(wasNetworkAvailable, isNetworkAvailable, doesHostRespond, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID));
//            }
//        }).start();
//    }


    //    public static class NetworkCheckStateImplicitEvent {
//
//        public final boolean isNetworkConnectionAvailable;
//
//        public NetworkCheckStateImplicitEvent(boolean isNetworkConnectionAvailable) {
//            this.isNetworkConnectionAvailable = isNetworkConnectionAvailable;
//        }
//    }

    static void handleNetworkState(final boolean wasNetworkAvailable, final boolean isNetworkAvailable, final NetworkState lastNetworkState, final NetworkState newNetworkState, String newNetworkID, final String lastNetworkID) {
        if (TextUtils.equals(lastNetworkID, newNetworkID) && lastNetworkState==newNetworkState && wasNetworkAvailable==isNetworkAvailable) {
            Log.i(NetworkStateHelper.class, "handleNetworkState(): same state -> ignoring");
            return;
        }
        // check if last lastNetworkID is WiFi's one (SSID/BSSID) and is it the same
        // or we switched from one WiFi network to another (networkId is changed)
        // cuz if its WiFi but networkId is changed - it is another WiFi network
        // and it could be a LAN via WiFi (with no Internet connection available)
        // boolean isWiFiNetworkChanged = !newNetworkID.equals(lastNetworkID);

        if ( isNetworkAvailable && !TextUtils.isEmpty(hostToCheck) ) {
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

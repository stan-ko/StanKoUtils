package com.stanko.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.stanko.tools.Log;

/**
 * Created by Stan Koshutsky <Stan.Koshutsky@gmail.com> on 06.04.2015.
 */
class NetworkStateReceiver extends BroadcastReceiver {

    //public static final String TAG="NetworkStateReceiver";

    public static NetworkState lastNetworkState;

    public static final String NETWORK_ID_4G = "4G";
    public static final String NETWORK_ID_MOBILE = "MOBILE";
    public static final String NETWORK_ID_OTHER = "OTHER";

    private final ConnectivityManager mConnectivityManager;
    private static Context sAppContext;

    private String lastNetworkID;
    private boolean wasNetworkAvailable;

    public NetworkStateReceiver(final Context context, ConnectivityManager connectivityManager) {
        sAppContext = context.getApplicationContext();
        mConnectivityManager = connectivityManager; //(ConnectivityManager) sAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            lastNetworkID = getNetworkID(sAppContext, activeNetworkInfo);
            Log.i("NetworkStateReceiver constructor: active/current NetworkID: " + lastNetworkID + " -> checkIfServerResponds()");
            lastNetworkState = NetworkState.NRGotNetwork;
            wasNetworkAvailable = true;
            NetworkStateHelper.handleNetworkState(false, true, lastNetworkState, lastNetworkState, null, lastNetworkID);
        } else {
            lastNetworkState = NetworkState.NRNoNetwork;
            Log.i("NetworkStateReceiver constructor: active/current NetworkID: " + lastNetworkID + " is NOT connected");
            NetworkStateHelper.handleNetworkState(false, false, lastNetworkState, lastNetworkState, null, null);
        }
    }


    @Override
    public synchronized void onReceive(final Context context, final Intent intent) {
        Log.i();

        final Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.i("Broadcast with NO EXTRAS - ignoring");
            return;
        }

        String newNetworkID;
        NetworkState newNetworkState;

        final NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            Log.i("ActiveNetwork is null -> NRNoNetwork");
            if (!wasNetworkAvailable && lastNetworkID == null && NetworkState.NRNoNetwork.equals(lastNetworkState)) {
                Log.i("No ActiveNetwork was registered already -> ignoring");
                return;
            }
            newNetworkState = NetworkState.NRNoNetwork;
            newNetworkID = null;
        } else if (extras.containsKey(ConnectivityManager.EXTRA_NETWORK_INFO)) {
            final NetworkInfo receivedNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            // if we got broadcast msg not about the active network - return
            if (activeNetworkInfo.getType() != receivedNetworkInfo.getType()) {
                Log.i("Broadcast is not about the ActiveNetwork. type: " + activeNetworkInfo.getType() + " vs received: " + receivedNetworkInfo.getType() + " - ignoring");
                return;
            } else {
                //final NetworkState wasCheckNetworkReason = checkNetworkReason;
                switch (receivedNetworkInfo.getState()) {
                    case CONNECTED:
                        newNetworkID = getNetworkID(sAppContext, receivedNetworkInfo);
                        // determine the type of network
                        switch (receivedNetworkInfo.getType()) {

                            //in AOS 2.1/API level7 4G/WIMAX is undefined in ConnectivityManager
                            case ConnectivityManager.TYPE_WIMAX:
                                newNetworkState = NetworkState.NRGotNetworkWiFi;
                                break;

                            case ConnectivityManager.TYPE_WIFI:
                                newNetworkState = NetworkState.NRGotNetworkWiFi;
                                break;

                            case ConnectivityManager.TYPE_MOBILE:
                                newNetworkState = NetworkState.NRGotNetworkMobile;
                                break;

                            default:
                                newNetworkState = NetworkState.NRGotNetworkOther;
                        }
                        break;

                    default:
//				case CONNECTING:
//				case DISCONNECTED:
//				case DISCONNECTING:
                        newNetworkState = NetworkState.NRNoNetwork;
//				case SUSPENDED:
//				case UNKNOWN:
//					checkNetwork(CheckNetworkReason.NRUnknown);
                        newNetworkID = null;
                        break;
                }
            }
        } else if (extras.containsKey(ConnectivityManager.EXTRA_NO_CONNECTIVITY) && !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
            newNetworkState = NetworkState.NRNoNetwork;
            newNetworkID = null;
        } else {
//	    		for(String key:extras.keySet()){
//	    			Log.i(TAG,"mIRNetwork: Bundle.key = "+key+", value = "+extras.get(key));
//	    		}
            newNetworkState = NetworkState.NRUnknown;
            newNetworkID = null;
        }

        boolean isLoggedNetwork = false;
        switch (newNetworkState) {
            case NRGotNetwork:
                Log.i("NetworkStateReceiver.onReceive(): NRGotNetwork");
            case NRGotNetworkMobile:
                Log.i("NetworkStateReceiver.onReceive(): NRGotNetworkMobile");
                isLoggedNetwork = true;
            case NRGotNetworkWiFi:
                if (!isLoggedNetwork) {
                    Log.i("NetworkStateReceiver.onReceive(): NRGotNetworkWiFi");
                    isLoggedNetwork = true;
                }
            case NRGotNetworkOther:
                if (!isLoggedNetwork) {
                    Log.i("NetworkStateReceiver.onReceive(): NRGotNetworkOther");
//                    isLoggedNetwork = true;
                }
                if (lastNetworkID != null && lastNetworkID.equals(newNetworkID)) {
                    NetworkStateHelper.handleNetworkState(wasNetworkAvailable, true, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
                    wasNetworkAvailable = true;
                } else {
                    NetworkStateHelper.handleNetworkState(wasNetworkAvailable, true, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
                    wasNetworkAvailable = true;
                }
                break;

//            case NRWiFiSwitched:
//                Log.i(this, "NetworkStateReceiver.onReceive(): NRWiFiSwitched -> checkIfServerResponds()");
//                handleNetworkState(wasNetworkAvailable, true, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
//                wasNetworkAvailable = true;
//                checkIfServerResponds();
//                break;

            default:
                Log.i("NetworkStateReceiver.onReceive(): case default: -> handleNetworkState(false)");
                NetworkStateHelper.handleNetworkState(wasNetworkAvailable, false, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
                wasNetworkAvailable = false;
        }

        lastNetworkID = newNetworkID;
        lastNetworkState = newNetworkState;
    }

    private String getNetworkID(final Context context, final NetworkInfo networkInfo) {
        if (networkInfo == null || context == null)
            return null;
        String newNetworkID;
        // determine the type of network
        switch (networkInfo.getType()) {

            case ConnectivityManager.TYPE_WIMAX:
                newNetworkID = NETWORK_ID_4G;
                break;

            case ConnectivityManager.TYPE_WIFI:
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                newNetworkID = wifiInfo.getSSID() + ':' + wifiInfo.getBSSID();
                break;

            case ConnectivityManager.TYPE_MOBILE:
                newNetworkID = NETWORK_ID_MOBILE;
                break;

            default:
                newNetworkID = NETWORK_ID_OTHER;
        }

        return newNetworkID;
    }

//    private void handleNetworkState(final boolean wasNetworkAvailable,
//                                    final boolean isNetworkAvailable,
//                                    final NetworkState lastNetworkState,
//                                    final NetworkState newNetworkState,
//                                    final String lastNetworkID,
//                                    final String newNetworkID)
//    {
//        NetworkStateHelper.handleNetworkState(wasNetworkAvailable, isNetworkAvailable, lastNetworkState, newNetworkState, lastNetworkID, newNetworkID);
//    }

}
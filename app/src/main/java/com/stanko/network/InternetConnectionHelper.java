package com.stanko.network;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

import com.stanko.tools.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * AsyncTask based class for checking  
 * Internet avalability and server reachabilty
 *
 * @author Stan Koshutsky
 *
 */
public class InternetConnectionHelper extends AsyncTask<Void, Void, Integer> {

    public static final int TIME_OUT=1000*1; //1s

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

    static enum InternetCheckResult{ICGotNetwork,ICUnknown};

    private WeakReference<ServerChecking> activity;
    private final String hostUrl;

    public final long startedMillis;

    private int result;

    // constructor accepts Activity which implements ServerChecking
    public <T extends Context & ServerChecking> InternetConnectionHelper(final T activity, final String hostUrl){
        super();
        this.activity = new WeakReference<ServerChecking>(activity);
        this.hostUrl = hostUrl;
        startedMillis = System.currentTimeMillis();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (hostUrl == null || hostUrl.length()==0)
            return null;

        return performCheck();
    }

    @Override
    protected void onPostExecute(final Integer result) {
        this.result = result;
        postResult();
    }

    public void postResult()
    {
        ServerChecking caller = activity.get();

        if (caller!=null)
        {
            if (caller instanceof Activity && ((Activity)caller).isFinishing() || isCancelled())
                return;
            caller.handleServerChekingResult(result);
        }

        activity.clear();

    }

    /**
     * method provides check if an Internet connection is available
     */
    private int performCheck() {
        ServerChecking caller = activity.get();
        if (!checkIsNetworkAvailable((Context)caller))
            return SC_NO_INTERNET_CONNECTION_AVAILABLE;
        else if (!checkIsServerReachable((Context)caller, hostUrl))
            return SC_SERVER_COULD_NOT_BE_REACHED;
        else
            return SC_OK
                    + (isInternetWiFi || isInternetWiMax ? SC_INTERNET_OVER_WIFI : 0)
                    + (isInternetMobile ? SC_INTERNET_OVER_MOBILE : 0)
                    + (isInternetOther ? SC_INTERNET_OVER_OTHER : 0);
    }


    public static boolean checkIsNetworkAvailable(final Context context) {

        if (context==null)
            return false;

        ConnectivityManager cm = null;
        try{
            cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e){} // like method not found?

        if (cm==null)
            return false;

        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getActiveNetworkInfo();
        } catch (Exception e){} // like method not found?

        isInternetOther	= networkInfo != null && networkInfo.isConnected();

        return isInternetOther;
    }

    /**
     * method checks if an Internet connection is available
     * 1) issue1: assumes the WiFi connection is an Internet connection
     * 2) issue2: fails if a WiFi connection requires a login/password
     * @return
     */
    public static boolean checkIsNetworkAvailableExt(final Context context) {

        isInternetWiFi 	= false;
        isInternetMobile= false;
        isInternetWiMax	= false;
        isInternetOther	= false;
        connectionType	= 0;

        if (context==null)
            return false;

        ConnectivityManager cm = null;
        try{
            cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e){}// like method not found?

        if (cm==null)
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

        if (niOther!=null)
            connectionType = niOther.getType();

        isInternetWiFi =   niWiFi!=null && niWiFi.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetMobile = niMobile!=null && niMobile.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetWiMax =  niWiMax!=null && niWiMax.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetOther =  niOther!=null && niOther.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;

        return (isInternetWiFi || isInternetWiMax || isInternetMobile  || isInternetOther);
    }

    /**
     * method checks if an Internet connection is available
     * 1) issue1: assumes the WiFi connection is an Internet connection
     * 2) issue2: fails if a WiFi connection requires a login/password
     * @return
     */
    public static boolean checkIsNetworkAvailableViaWiFi(final Context context) {

        isInternetWiFi 	= false;
        isInternetMobile= false;
        isInternetWiMax	= false;
        isInternetOther	= false;
        connectionType	= 0;

        if (context==null)
            return false;

        ConnectivityManager cm = null;
        try{
            cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e){}// like method not found?

        if (cm==null)
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

        if (niOther!=null)
            connectionType = niOther.getType();

        isInternetWiFi =   niWiFi!=null && niWiFi.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetMobile = niMobile!=null && niMobile.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetWiMax =  niWiMax!=null && niWiMax.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;
        isInternetOther =  niOther!=null && niOther.isConnected(); //.getState() == NetworkInfo.State.CONNECTED;

        return (isInternetWiFi || isInternetWiMax);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    // hostName parameter must be a host url without http:// prefix
    public static boolean checkIsServerReachable(final Context context, final String hostName) {

        // check if there is http:// at the start of hostName
        String host = hostName.trim();
        if (host.contains("://"))
            host = host.substring(host.indexOf("://")+3); // cut off http://, https://, ftp:// etc

        final int serverIP = getHostIntIP(host);
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

            else if (isInternetOther){
                connectionType = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType();
                isInternetOther = cm.requestRouteToHost(connectionType, serverIP);
            }

            return ( isInternetWiFi || isInternetWiMax || isInternetMobile || isInternetOther );

        } else
            return checkHostByConnection(hostName);
    }

    public static InetAddress getHostIP(final String hostName) {
        // check if there is http:// at the start of hostName
        String host = null;
        if (hostName.indexOf("http://")==0)
            host = hostName.substring(7);
        else
            host = hostName;

        InetAddress inetAddress=null;
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;
        }
        return inetAddress;
    }

    public static int getHostIntIP(String hostname) {

        final InetAddress inetAddress = getHostIP(hostname);

        if (inetAddress==null)
            return 0;

        final byte[] addrBytes = inetAddress.getAddress();
        final int addr = (addrBytes[3]&0xff)  << 24
                | (addrBytes[2]&0xff) << 16
                | (addrBytes[1]&0xff) << 8
                |  addrBytes[0]&0xff;
        return addr;
    }

    public static boolean checkHostByConnection(String hostUrl) {

        // adding http:// if its just host name
        if (hostUrl.indexOf("http://")!=0)
            hostUrl = "http://" + hostUrl;


        boolean isReachable = false;
        try {

            URL url = new URL(hostUrl);

            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", "Android Application");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 5); // mTimeout is in seconds
            urlc.connect();

            if (urlc.getResponseCode() == 200)
                isReachable = true;

        } catch (MalformedURLException e) {
            Log.e("ServerChecker", e);
        } catch (IOException e) {
            Log.e("ServerChecker",e);
        }
        return isReachable;
    }


    public void detach()
    {
        activity.clear();// = null;
    }

    public <T extends Activity & ServerChecking> void attach(final T activity)
    {
        this.activity = new WeakReference<ServerChecking>(activity);
        if (getStatus() == AsyncTask.Status.FINISHED)
            postResult();
    }

    public static boolean isInternetMobile(){
        return isInternetMobile;
    }

    // интерфейс callback-метода для тех, кто хочет АcинкТаском получить результат проверки доступности сервера
    public interface ServerChecking {
        public void handleServerChekingResult(final int result);
    }

}
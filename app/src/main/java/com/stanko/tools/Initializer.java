package com.stanko.tools;

import android.content.Context;

/**
 * Authors:
 * Stan Koshutsky
 */

public class Initializer {

    private static Context appContext;
    private static String hostToPing;

    public static void init(final Context context){
        appContext = context.getApplicationContext();
    }

    public static void init(final Context context, final String _hostToPing){
        appContext = context.getApplicationContext();
        hostToPing = _hostToPing;
    }

    public static Context getAppContext(){
        return appContext;
    }

    public static String getHostToPing(){
        return hostToPing;
    }

}

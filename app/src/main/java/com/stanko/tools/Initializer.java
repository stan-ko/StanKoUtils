package com.stanko.tools;

import android.content.Context;
import android.content.res.Resources;

/**
 * This class is used to initialize with context following classes:
 * DeviceInfo
 * FontHelperBase
 * LocaleHelper
 * ResHelper
 * SDCardHelper
 * SharedPrefsHelper
 *
 * So if you initialize this class you don't have to initialize listed classes. Each of listed
 * classes will be initialized on demand, when its method which requires Context be called.
 *
 * Authors:
 * Stan Koshutsky
 */

public class Initializer {

    private static Context sAppContext;
    private static String sHostToPing;

    public static void init(final Context context){
        sAppContext = context.getApplicationContext();
    }

    public static void init(final Context context, final String _hostToPing){
        sAppContext = context.getApplicationContext();
        sHostToPing = _hostToPing;
    }

    public static Context getAppContext(){
        if (sAppContext==null)
            Log.e("You should initialize Initializer class with Context first. Otherwise NPE will be thrown");
        return sAppContext;
    }

    public static String getsHostToPing(){
        return sHostToPing;
    }

    public static Resources getResources() {
        return sAppContext.getResources();
    }
}

package com.stanko.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * (c)Nova Poshta by theMakeApp
 * <p/>
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
 * Basil Miller
 * <p/>
 * to make getString() and other app wide accessible
 */

public class ResHelper {

    private static Context sAppContext;
    private static Resources sResources;
    private static DisplayMetrics sDisplayMetrics;

    public static void init(final Context context) {
        if (sAppContext == null && context != null) {
            sAppContext = context.getApplicationContext();
            sResources = sAppContext.getResources();
            sDisplayMetrics = sResources.getDisplayMetrics();
        }
    }

    public static String getString(final int resId) {
        initOnDemand();
        return sResources.getString(resId);
    }

    public static String getString(final int resId, final Object... args) {
        initOnDemand();
        if (args == null || args.length == 0)
            return getString(resId);
        else
            return sResources.getString(resId, args);
    }

    public static String[] getStringArray(final int arrayResId) {
        initOnDemand();
        return sResources.getStringArray(arrayResId);
    }

    public static String getQuantityString(final int resId, final int quantity) {
        initOnDemand();
        if (resId == 0)
            return null;
        else
            return sResources.getQuantityString(resId, quantity);
    }

    public static String getQuantityString(final int resId, final int quantity, Object... formatArgs) {
        initOnDemand();
        if (resId == 0)
            return null;
        else
            return sResources.getQuantityString(resId, quantity, formatArgs);
    }

    public static Drawable getDrawable(final int resId) {
        initOnDemand();
        return sResources.getDrawable(resId);
    }

    public static int getColor(final int colorResId) {
        initOnDemand();
        return sResources.getColor(colorResId);
    }

    public static float getDimension(final int dimensResId) {
        initOnDemand();
        return sResources.getDimension(dimensResId);
    }

    public static int getDimensPx(final int dimensResId) {
        initOnDemand();
        return sResources.getDimensionPixelSize(dimensResId);
    }

    public static DisplayMetrics getDisplayMetrics() {
        initOnDemand();
        return sDisplayMetrics;
    }

    public static int getInteger(final int intResId) {
        initOnDemand();
        return sResources.getInteger(intResId);
    }

    public static boolean getBoolean(final int booleanResId) {
        initOnDemand();
        return sResources.getBoolean(booleanResId);
    }

    public static int[] getIntArray(final int arrayResId) {
        initOnDemand();
        return sResources.getIntArray(arrayResId);
    }

    public static int getIdentifier(final String name, final String defType, final String defPackage) {
        initOnDemand();
        return sResources.getIdentifier(name, defType, defPackage);
    }

    public static InputStream openRawResource(final int rawResId) {
        initOnDemand();
        return sResources.openRawResource(rawResId);
    }

    public static int getResId(final String resName, final Class<?> c) {
        initOnDemand();
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean isResourceAvailable(int resId) {
        initOnDemand();
        try {
            final String name = sResources.getResourceName(resId);
            if (name == null || !name.startsWith(sAppContext.getPackageName())) {
                return false;
            }
            return true; //Didn't catch exception so id is in res
        } catch (Resources.NotFoundException e) {
            return false;
        }
    }

    private static void initOnDemand() {
        // init on demand
        if (sAppContext == null && Initializer.getAppContext() != null) {
            init(Initializer.getAppContext());
        }
    }

}

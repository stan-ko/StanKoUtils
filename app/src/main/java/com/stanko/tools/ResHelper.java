package com.stanko.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.InputStream;

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
        return sResources.getString(resId);
    }

    public static String getString(final int resId, final Object... args) {
        if (args == null || args.length==0)
            return getString(resId);
        else
            return sResources.getString(resId, args);
    }

    public static Drawable getDrawable(final int resId) {
        return sResources.getDrawable(resId);
    }

    public static int getColor(final int colorResId) {
        return sResources.getColor(colorResId);
    }

    public static float getDimension(final int dimensResId) {
        return sResources.getDimension(dimensResId);
    }

    public static int getDimensPx(final int dimensResId) {
        return sResources.getDimensionPixelSize(dimensResId);
    }

    public static DisplayMetrics getDisplayMetrics() {
        return sDisplayMetrics;
    }

    public static int getInteger(final int intResId) {
        return sResources.getInteger(intResId);
    }

    public static String[] getStringArray(final int arrayResId) {
        return sResources.getStringArray(arrayResId);
    }

    public static boolean getBoolean(final int booleanResId) {
        return sResources.getBoolean(booleanResId);
    }

    public static int[] getIntArray(final int arrayResId) {
        return sResources.getIntArray(arrayResId);
    }

    public static int getIdentifier(final String name, final String defType, final String defPackage) {
        return sResources.getIdentifier(name, defType, defPackage);
    }

    public static InputStream openRawResource(final int rawResId) {
        return sResources.openRawResource(rawResId);
    }

    public static String getQuantityString(final int resId, final int quantity) {
        if (resId == 0)
            return null;
        else
            return sResources.getQuantityString(resId, quantity);
    }

    public static String getQuantityString(final int resId, final int quantity, Object... formatArgs) {
        if (resId == 0)
            return null;
        else
            return sResources.getQuantityString(resId, quantity, formatArgs);
    }

}

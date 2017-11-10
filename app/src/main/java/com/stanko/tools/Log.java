package com.stanko.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Locale;

/*
* Created by Stan Koshutsky <Stan.Koshutsky@gmail.com>
*/
public class Log {

    private static String packageNameToCutOff = "";
    private static boolean doPackageCutOff;
    private static boolean isDebuggable;

    /**
     * do init for cutting off the app's package name to shorten
     * the resulting Tag string length
     *
     * @param context
     */
    public static void init(final Context context) {
        init(context.getPackageName());
        isDebuggable = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * do init for cutting off the app's package name to shorten
     * the resulting Tag string length
     *
     * @param packageNameToCutOff
     */
    private static void init(final String packageNameToCutOff) {
        if (doPackageCutOff = !TextUtils.isEmpty(packageNameToCutOff))
            Log.packageNameToCutOff = packageNameToCutOff.lastIndexOf(".") == packageNameToCutOff.length() - 1 ? packageNameToCutOff : packageNameToCutOff + ".";
    }

    /**
     * method shortens the Tag by cutting off the app's package name
     * if init was fired previously
     *
     * @param tagClass
     * @return
     */
    public static String getLogTag(final Class<?> tagClass) {
        if (doPackageCutOff)
            return tagClass.getName().replace(packageNameToCutOff, "");
        return tagClass.getName();
    }

    /**
     * method shortens The tag by cutting off the app's package name
     * if init was fired previously
     *
     * @param tagObject
     * @return
     */
    public static String getLogTag(Object tagObject) {
        if (doPackageCutOff)
            return tagObject.getClass().getName().replace(packageNameToCutOff, "");
        return tagObject.getClass().getName();
    }


    // Method as logTag
    public static void v() {
        if (isDebuggable) {
            final String[] classDotMethod = getClassDotMethod();
            android.util.Log.v(classDotMethod[0], classDotMethod[1]);
        }
    }

    public static void d() {
        if (isDebuggable) {
            final String[] classDotMethod = getClassDotMethod();
            android.util.Log.d(classDotMethod[0], classDotMethod[1]);
        }
    }

    public static void i() {
        if (isDebuggable) {
            final String[] classDotMethod = getClassDotMethod();
            android.util.Log.i(classDotMethod[0], classDotMethod[1]);
        }
    }

    public static void w() {
//        if (isDebuggable){
        final String[] classDotMethod = getClassDotMethod();
        android.util.Log.w(classDotMethod[0], classDotMethod[1]);
//        }
    }

    public static void e() {
//        if (isDebuggable){
        final String[] classDotMethod = getClassDotMethod();
        android.util.Log.e(classDotMethod[0], classDotMethod[1]);
//        }
    }


    // Method as logTag
    public static void v(final String msg) {
        if (isDebuggable)
            android.util.Log.v(getMethodName(), msg);
    }

    public static void d(final String msg) {
        if (isDebuggable)
            android.util.Log.d(getMethodName(), msg);
    }

    public static void i(final String msg) {
        if (isDebuggable)
            android.util.Log.i(getMethodName(), msg);
    }

    public static void w(final String msg) {
        android.util.Log.w(getMethodName(), msg);
    }

    public static void e(final String msg) {
        android.util.Log.e(getMethodName(), msg);
    }

    public static void e(final Exception e) {
        android.util.Log.e(getMethodName(), e.getMessage(), e);
    }

    public static void e(final Throwable e) {
        android.util.Log.e(getMethodName(), e.getMessage(), e);
    }

    // String as logTag
    public static void v(final String logTag, final String msg) {
        if (isDebuggable)
            android.util.Log.v(logTag, msg);
    }

    public static void d(final String logTag, final String msg) {
        if (isDebuggable)
            android.util.Log.d(logTag, msg);
    }

    public static void i(final String logTag, final String msg) {
        if (isDebuggable)
            android.util.Log.i(logTag, msg);
    }

    public static void w(final String logTag, final String msg) {
        android.util.Log.w(logTag, msg);
    }

    public static void e(final String logTag, final String msg) {
        android.util.Log.e(logTag, msg);
    }

    public static void e(final String logTag, final Exception e) {
        e(logTag, e.getMessage(), e);
    }

    public static void e(final String logTag, final Throwable e) {
        e(logTag, e.getMessage(), e);
    }

    public static void e(final String logTag, final String msg, final Exception e) {
        android.util.Log.e(logTag, msg, e);
    }

    public static void e(final String logTag, final String msg, final Throwable e) {
        android.util.Log.e(logTag, msg, e);
    }

    // Object as logTag => Object.getClass().getName()
    public static void v(final Object logTag, final String msg) {
        if (isDebuggable)
            v(getLogTag(logTag), msg);
    }

    public static void d(final Object logTag, final String msg) {
        if (isDebuggable)
            d(getLogTag(logTag), msg);
    }

    public static void i(final Object logTag, final String msg) {
        if (isDebuggable)
            i(getLogTag(logTag), msg);
    }

    public static void w(final Object logTag, final String msg) {
        w(getLogTag(logTag), msg);
    }

    public static void e(final Object logTag, final String msg) {
        e(getLogTag(logTag), msg);
    }

    public static void e(final Object logTag, final String msg, final Exception e) {
        e(getLogTag(logTag), msg, e);
    }

    public static void e(final Object logTag, final String msg, final Throwable e) {
        e(getLogTag(logTag), msg, e);
    }

    public static void e(final Object logTag, final Exception e) {
        e(getLogTag(logTag), e);
    }

    public static void e(final Object logTag, final Throwable e) {
        e(getLogTag(logTag), e);
    }


    // Class as logTag => Class.getName()
    public static void v(final Class<?> logTag, final String msg) {
        if (isDebuggable)
            v(getLogTag(logTag), msg);
    }

    public static void d(final Class<?> logTag, final String msg) {
        if (isDebuggable)
            d(getLogTag(logTag), msg);
    }

    public static void i(final Class<?> logTag, final String msg) {
        if (isDebuggable)
            i(getLogTag(logTag), msg);
    }

    public static void w(final Class<?> logTag, final String msg) {
        w(getLogTag(logTag), msg);
    }

    public static void e(final Class<?> logTag, final String msg) {
        e(getLogTag(logTag), msg);
    }

    public static void e(final Class<?> logTag, final String msg, final Exception e) {
        e(getLogTag(logTag), msg, e);
    }

    public static void e(final Class<?> logTag, final String msg, final Throwable e) {
        e(getLogTag(logTag), msg, e);
    }

    public static void e(final Class<?> logTag, final Exception e) {
        e(getLogTag(logTag), e);
    }

    public static void e(final Class<?> logTag, final Throwable e) {
        e(getLogTag(logTag), e);
    }

    /**
     * Returns method name from where method containing Log was called (like previous level
     * corresponding to stack)
     *
     * @return method name
     */
    public static String getMethodName() {
        String methodName = "U/D";
        StackTraceElement[] stackTraceElements;
        StackTraceElement stackTraceElement;
        stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements == null || stackTraceElements.length < 5) {
            stackTraceElements = (new Throwable()).getStackTrace();
            if (stackTraceElements == null || stackTraceElements.length < 3) {
                return methodName;
            } else {
                stackTraceElement = stackTraceElements[2];
            }
        } else {
            stackTraceElement = stackTraceElements[4];
        }
        if (stackTraceElement != null) {
            methodName = String.format(Locale.US, "%s.%s():%d", stackTraceElement.getFileName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        }
        return methodName;
    }

    /**
     * Returns method name from where method containing Log was called (like previous level
     * corresponding to stack) formatted as Class.methodName
     *
     * @return
     */
    public static String[] getClassDotMethod() {
        final String[] classMethod = new String[]{"U/D", "U/D"};
        //String methodName = "U/D";
        StackTraceElement[] stackTraceElements;
        StackTraceElement stackTraceElement;
        stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements == null || stackTraceElements.length < 5) {
            stackTraceElements = (new Throwable()).getStackTrace();
            if (stackTraceElements == null || stackTraceElements.length < 3) {
                return classMethod;
            } else {
                stackTraceElement = stackTraceElements[2];
            }
        } else {
            stackTraceElement = stackTraceElements[4];
        }
        if (stackTraceElement != null) {
            classMethod[0] = stackTraceElement.getFileName();
            classMethod[1] = String.format(Locale.US, "%s():%d", stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        }
        return classMethod;
    }

    private final static int DIVIDER_LENGTH = 120;

    public static void logIntent(final Intent intent, String title) {
        if (!isDebuggable)
            return;

        final StringBuilder divider = new StringBuilder();

        final int titleLength;
        if (title == null) {
            title = "";
            titleLength = 0;
        } else {
            title = String.format(" %s ", title);
            titleLength = title.length();
        }
        for (int i = 0; i < DIVIDER_LENGTH - 15 - titleLength; i++) divider.append("─");

        d("┌───────────────" + title + divider.toString());
        if (intent != null && intent.getExtras() != null) {
            final Bundle bundle = intent.getExtras();
            logBundle(null, bundle);
        } else {
            d("├ NO EXTRAS");
        }
        divider.insert(0, "└───────────────");
        for (int i = 0; i < titleLength; i++) divider.append("─");
        Log.d(divider.toString());
    }

    public static void logBundle(final Bundle bundle, String title) {
        if (!isDebuggable)
            return;

        final StringBuilder divider = new StringBuilder();

        final int titleLength;
        if (title == null) {
            title = "";
            titleLength = 0;
        } else {
            title = String.format(" %s ", title);
            titleLength = title.length();
        }
        for (int i = 0; i < DIVIDER_LENGTH - 15 - titleLength; i++) divider.append("─");

        d("┌───────────────" + title + divider.toString());
        logBundle(null, bundle);
        divider.insert(0, "└───────────────");
        for (int i = 0; i < titleLength; i++) divider.append("─");
        Log.d(divider.toString());
    }

    private static void logBundle(String parentKey, final Bundle bundle) {
        if (bundle != null) {
            if (bundle.size() != 0) {
                for (String key : bundle.keySet()) {
                    final Object value = bundle.get(key);
                    if (parentKey != null) key = String.format("%s.%s", parentKey, key);
                    if (value != null) {
                        if (value instanceof Bundle) logBundle(key, (Bundle) value);
                        else {
                            d(String.format("├ KEY: [%s] ─── %s (%s)",
                                    key, value.toString(), value.getClass().getName())
                            );
                        }
                    } else
                        d(String.format("├ KEY: [%s] ─── null", key));
                }
            } else
                d(String.format("├ KEY: [%s] ─── NO EXTRAS", parentKey));
        } else {
            if (parentKey == null)
                d("├ NO EXTRAS");
            else
                d(String.format("├ %s NO EXTRAS", parentKey));
        }
    }

}

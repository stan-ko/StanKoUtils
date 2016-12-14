package com.stanko.tools;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

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
        if (isDebuggable){
            final String [] classDotMethod = getClassDotMethod();
            android.util.Log.v(classDotMethod[0],classDotMethod[1]);
        }
    }

    public static void d() {
        if (isDebuggable){
            final String [] classDotMethod = getClassDotMethod();
            android.util.Log.d(classDotMethod[0],classDotMethod[1]);
        }
    }

    public static void i() {
        if (isDebuggable){
            final String [] classDotMethod = getClassDotMethod();
            android.util.Log.i(classDotMethod[0],classDotMethod[1]);
        }
    }

    public static void w() {
//        if (isDebuggable){
            final String [] classDotMethod = getClassDotMethod();
            android.util.Log.w(classDotMethod[0],classDotMethod[1]);
//        }
    }

    public static void e() {
//        if (isDebuggable){
            final String [] classDotMethod = getClassDotMethod();
            android.util.Log.e(classDotMethod[0],classDotMethod[1]);
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
            methodName = String.format("%s.%s():%d", stackTraceElement.getFileName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        }
        return methodName;
    }

    public static String[] getClassDotMethod() {
        final String[] classMethod = new String[]{"U/D","U/D"};
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
            classMethod[1] = String.format("%s():%d", stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        }
        return classMethod;
    }

}

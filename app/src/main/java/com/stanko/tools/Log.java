package com.stanko.tools;

import android.content.Context;
import android.text.TextUtils;

import com.stanko.BuildConfig;

public class Log {
	
	private static String packageNameToCutOff="";
	private static boolean doPackageCutOff;
	
	/**
	 * do init for cutting off the app's package name to shorten 
	 * the resulting Tag string length
	 * @param context
	 */
	public static void init(final Context context){
		init(context.getPackageName());
	}
	/**
	 * do init for cutting off the app's package name to shorten 
	 * the resulting Tag string length
	 * @param packageNameToCutOff
	 */
	public static void init(final String packageNameToCutOff){
		if ( doPackageCutOff = !TextUtils.isEmpty(packageNameToCutOff) )
			Log.packageNameToCutOff = packageNameToCutOff.lastIndexOf(".")==packageNameToCutOff.length()-1 ? packageNameToCutOff : packageNameToCutOff+".";
	}
	
	/**
	 * method shortens the Tag by cutting off the app's package name 
	 * if init was fired previously
	 * @param tagClass
	 * @return
	 */
	public static String getTag(final Class<?> tagClass){
		if (doPackageCutOff)
			return tagClass.getName().replace(packageNameToCutOff, "");
		return tagClass.getName();
	}

	/**
	 * method shortens The tag by cutting off the app's package name 
	 * if init was fired previously
	 * @param tagObject
	 * @return
	 */
	public static String getTag(Object tagObject){
		if (doPackageCutOff)
			return tagObject.getClass().getName().replace(packageNameToCutOff, "");
        return tagObject.getClass().getName();
	}
	
	
	// String as LOGTAG
	public static void v(final String LOGTAG, final String msg){
        if (BuildConfig.DEBUG)
        	android.util.Log.v(LOGTAG,msg);
	}

	public static void d(final String LOGTAG, final String msg){
        if (BuildConfig.DEBUG)
        	android.util.Log.d(LOGTAG,msg);
	}

	public static void i(final String LOGTAG, final String msg){
        if (BuildConfig.DEBUG)
        	android.util.Log.i(LOGTAG,msg);
	}

	public static void w(final String LOGTAG, final String msg){
        if (BuildConfig.DEBUG)
        	android.util.Log.w(LOGTAG,msg);
	}

	public static void e(final String LOGTAG, final String msg){
//        if (BuildConfig.DEBUG)
        	android.util.Log.e(LOGTAG,msg);
	}

	public static void e(final String LOGTAG, final Exception e){
        e(LOGTAG,e.getMessage(),e);
	}
	public static void e(final String LOGTAG, final String msg, final Exception e){
//        if (BuildConfig.DEBUG)
        	android.util.Log.e(LOGTAG,msg,e);
	}

	
	// Object as LOGTAG => Object.getClass().getName()
	public static void v(final Object LOGTAG, final String msg){
        v(getTag(LOGTAG),msg);
	}

	public static void d(final Object LOGTAG, final String msg){
        d(getTag(LOGTAG),msg);
	}

	public static void i(final Object LOGTAG, final String msg){
        i(getTag(LOGTAG),msg);
	}

	public static void w(final Object LOGTAG, final String msg){
        w(getTag(LOGTAG),msg);
	}

	public static void e(final Object LOGTAG, final String msg){
        e(getTag(LOGTAG),msg);
	}

	public static void e(final Object LOGTAG, final String msg, final Exception e){
        e(getTag(LOGTAG),msg,e);
	}

	public static void e(final Object LOGTAG, final Exception e){
        e(getTag(LOGTAG),e);
	}

	
	// Class as LOGTAG => Class.getName()
	public static void v(final Class<?> LOGTAG, final String msg){
        v(getTag(LOGTAG),msg);
	}

	public static void d(final Class<?> LOGTAG, final String msg){
        d(getTag(LOGTAG),msg);
	}

	public static void i(final Class<?> LOGTAG, final String msg){
        i(getTag(LOGTAG),msg);
	}

	public static void w(final Class<?> LOGTAG, final String msg){
        w(getTag(LOGTAG),msg);
	}

	public static void e(final Class<?> LOGTAG, final String msg){
        e(getTag(LOGTAG),msg);
	}

	public static void e(final Class<?> LOGTAG, final String msg, final Exception e){
        e(getTag(LOGTAG),msg,e);
	}

	public static void e(final Class<?> LOGTAG, final Exception e){
        e(getTag(LOGTAG),e);
	}

}

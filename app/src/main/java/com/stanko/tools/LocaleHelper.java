package com.stanko.tools;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {

    /**
     * Applies the new Locale for the application on the fly
     *
     * Note:
     * This method expects Initializer class to be initialized with context
     *
     * @param languageCode - ISO language code, applicable for instantiating Locale
     */
    public static void setAppLocale(final String languageCode) {
        setAppLocale(Initializer.getsAppContext(), new Locale(languageCode));
    }

    /**
     * Applies the new Locale for the application on the fly
     *
     * Note:
     * This method expects Initializer class to be initialized with context
     *
     * @param countryCode - ISO country code, applicable for instantiating Locale
     * @param languageCode - ISO language code, applicable for instantiating Locale
     */
    public static void setAppLocale(final String countryCode, final String languageCode) {
        setAppLocale(Initializer.getsAppContext(), new Locale(languageCode, countryCode));
    }

    /**
     * Applies the new Locale for the application on the fly
     *
     * @param context - any Context
     * @param languageCode - ISO language code, applicable for instantiating Locale
     */
    public static void setAppLocale(Context context, final String languageCode) {
        setAppLocale(context, new Locale(languageCode));
    }

    /**
     * Applies the new Locale for the application on the fly
     *
     * @param context - any Context
     * @param countryCode - ISO country code, applicable for instantiating Locale
     * @param languageCode - ISO language code, applicable for instantiating Locale
     */
    public static void setAppLocale(Context context, final String countryCode, final String languageCode) {
        setAppLocale(context, new Locale(languageCode, countryCode));
    }

    /**
     * Applies the new Locale for the application on the fly
     *
     * @param context - any Context
     * @param locale2set - Locale instance to apply
     */
    public static void setAppLocale(Context context, final Locale locale2set) {
        Locale.setDefault(locale2set);
        final Resources resources = context.getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        final Configuration configuration = resources.getConfiguration();
        configuration.locale = locale2set;
        resources.updateConfiguration(configuration, displayMetrics);
//		Configuration config = new Configuration();
//		config.locale = locale2set;
//		context.getApplicationContext().getResources().updateConfiguration(config, context.getApplicationContext().getResources().getDisplayMetrics());
    }

}

package com.stanko.tools;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class LocaleHelper {
	
	public static void setAppLocale(Context context, final String languageCode) {
		setAppLocale(context,new Locale(languageCode));
	}	
	
	public static void setAppLocale(Context context, final String countryCode, final String languageCode) {
		setAppLocale(context,new Locale(languageCode,countryCode));
	}	

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

package com.stanko.tools;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;

public class LocaleHelper {
	
	public static void setAppLocale(Context context, final String languageCode) {
		setAppLocale(context,new Locale(languageCode));
	}	
	
	public static void setAppLocale(Context context, final String countryCode, final String languageCode) {
		setAppLocale(context,new Locale(languageCode,countryCode));
	}	

	public static void setAppLocale(Context context, final Locale locale2set) {
		Locale.setDefault(locale2set);
		Configuration config = new Configuration();
		config.locale = locale2set;
		context.getApplicationContext().getResources().updateConfiguration(config, context.getApplicationContext().getResources().getDisplayMetrics());
	}

}

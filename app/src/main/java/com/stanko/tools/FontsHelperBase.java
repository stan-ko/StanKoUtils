package com.stanko.tools;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontsHelperBase {
	public final static String FONTHELPER_FONTS_PATH = "fonts/";
	
	private static final Map<String,Typeface> lastUsedFonts = new HashMap<String,Typeface>();
	private static String 	lastUsedFontFileName;
	private static Typeface lastUsedFont;
	
	public static Typeface getFont(final Context context, final String fontFileName) {
		
		if ( fontFileName.equals(lastUsedFontFileName) )
			return lastUsedFont;
		
		if (lastUsedFontFileName != null)
			lastUsedFonts.put(lastUsedFontFileName, lastUsedFont);
		
		lastUsedFontFileName = fontFileName;
		lastUsedFont = lastUsedFonts.get(fontFileName);
		
		if (lastUsedFont == null)
			lastUsedFont = Typeface.createFromAsset(context.getAssets(), FONTHELPER_FONTS_PATH + fontFileName);
		
		return lastUsedFont;
	}

	public static void setFontToView(final TextView view, final String font) {
		final Typeface mFont = getFont(view.getContext(), font);
		if (mFont != null)
			view.setTypeface(mFont);
	}

	public static void setFontToViews(final Typeface mFont, final TextView... views) {
		if (mFont != null)
			for (TextView view : views)
				if (view!=null)
					view.setTypeface(mFont);
	}
	
	public static float getTextSizeFromDimens(Context context, int resId){
		return getTextSizeFromDimens(context.getResources(), resId);
	}
	
	public static float getTextSizeFromDimens(Resources resources, int resId){
		if (resId==0)
			return 0;
		final String textSizeAttribute = resources.getString(resId).replaceAll("[^\\d\\.]+", "");
		float textSize = 0;
		try {
			textSize = Float.valueOf(textSizeAttribute);
		} catch (NumberFormatException ignored){}
		return textSize;
	}
	
/*

	public final static String FONTHELPER_FONT_NAME_MUSEO = "MuseoCyrl-500.otf";
	public final static String FONTHELPER_FONT_NAME_INTRO = "Intro-Bold-Caps.otf";

	public static void setIntroFontToViews(final TextView... views) {
		if (views.length==0)
			return;
		final Typeface mFont = getFont(views[0].getContext(), FONTHELPER_FONT_NAME_INTRO);
		setFontToViews(mFont, views);
	}

	public static void setMuseoCyrlFontToViews(final TextView... views) {
		if (views.length==0)
			return;
		final Typeface mFont = getFont(views[0].getContext(), FONTHELPER_FONT_NAME_MUSEO);
		setFontToViews(mFont, views);
	}

	public static void setIntroFontToView(final TextView view) {
		if (view == null)
			return;
		final Typeface mFont = getFont(view.getContext(), FONTHELPER_FONT_NAME_INTRO);
		view.setTypeface(mFont);
	}

	public static void setMuseoCyrlFontToView(final TextView view) {
		if (view == null)
			return;
		final Typeface mFont = getFont(view.getContext(), FONTHELPER_FONT_NAME_MUSEO);
		view.setTypeface(mFont);
	}
*/
}

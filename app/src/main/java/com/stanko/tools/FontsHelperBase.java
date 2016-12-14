package com.stanko.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class FontsHelperBase {
    public final static String FONTHELPER_FONTS_PATH = "fonts/";

    private static final Map<String, Typeface> lastUsedFonts = new HashMap<String, Typeface>();
    private static String lastUsedFontFileName;
    private static Typeface lastUsedFont;

    public static Typeface getFont(final Context context, final String fontFileName) {

        if (TextUtils.equals(fontFileName, lastUsedFontFileName) && lastUsedFont != null)
            return lastUsedFont;

        lastUsedFontFileName = fontFileName;
        lastUsedFont = lastUsedFonts.get(fontFileName);
        if (lastUsedFont == null) {
            try {
                lastUsedFont = Typeface.createFromAsset(context.getAssets(), FONTHELPER_FONTS_PATH + fontFileName);
                lastUsedFonts.put(lastUsedFontFileName, lastUsedFont);
            } catch (Throwable e) { //java.lang.RuntimeException: native typeface cannot be made
                e.printStackTrace();
                lastUsedFont = Typeface.defaultFromStyle(Typeface.NORMAL);
            }
        }

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
                if (view != null)
                    view.setTypeface(mFont);
    }

    public static float getTextSizeFromDimens(Context context, int resId) {
        return getTextSizeFromDimens(context.getResources(), resId);
    }

    public static float getTextSizeFromDimens(Resources resources, int resId) {
        if (resId == 0)
            return 0;
        final String textSizeAttribute = resources.getString(resId).replaceAll("[^\\d\\.]+", "");
        float textSize = 0;
        try {
            textSize = Float.valueOf(textSizeAttribute);
        } catch (NumberFormatException ignored) {
        }
        return textSize;
    }
}

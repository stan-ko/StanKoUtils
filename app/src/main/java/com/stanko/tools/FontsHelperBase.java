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

    /**
     * Instantiates, caches and returns Typeface for given font file. If font could not be
     * instantiated for some reason Typeface.NORMAL (a regular) font will be returned.
     * The font file must be located in assets/fonts/ directory of a project. Don't use camelCase
     * in fonts file names.
     *
     * @param context      - any context
     * @param fontFileName - a target font file name from assets/fonts/ directory of a project
     * @return Typeface
     */
    public static Typeface getFont(final Context context, final String fontFileName) {

        if (TextUtils.equals(fontFileName, lastUsedFontFileName) && lastUsedFont != null)
            return lastUsedFont;

        lastUsedFontFileName = fontFileName;
        lastUsedFont = lastUsedFonts.get(fontFileName); // get from cache
        if (lastUsedFont == null) {
            try {
                lastUsedFont = Typeface.createFromAsset(context.getAssets(), FONTHELPER_FONTS_PATH + fontFileName);
                lastUsedFonts.put(lastUsedFontFileName, lastUsedFont);
            } catch (Throwable e) { //java.lang.RuntimeException: native typeface cannot be made
                e.printStackTrace();
            }
        }
        // CostYL: if desired font could not be instantiated - set default font.NORMAL
        if (lastUsedFont == null)
            lastUsedFont = Typeface.defaultFromStyle(Typeface.NORMAL);

        return lastUsedFont;
    }

    /**
     * Instantiates, caches and returns Typeface for given font file. If font could not be
     * instantiated for some reason Typeface.NORMAL (a regular) font will be returned.
     * The font file must be located in assets/fonts/ directory of a project. Don't use camelCase
     * in fonts file names.
     * Note:
     * This method expects Initializer class to be initialized with context
     *
     * @param fontFileName - a target font file name from assets/fonts/ directory of a project
     * @return Typeface
     */
    public static Typeface getFont(final String fontFileName) {
        return getFont(Initializer.getsAppContext(), fontFileName);
    }


    /**
     * Instantiates desired font and applies this font to a given View.
     *
     * @param view - any TextView or its successor like Button, EditText and so on
     * @param font - target font file name from assets/fonts/ directory of a project
     */
    public static void setFontToView(final TextView view, final String font) {
        final Typeface mFont = getFont(view.getContext(), font);
        if (mFont != null)
            view.setTypeface(mFont);
    }

    /**
     * Instantiates desired font and applies this font to given views. Views could be of mixed type
     * like [TextView, EditText, TextView, TextView, Button] etc.
     *
     * @param views - any TextView or its successor like Button, EditText and so on
     * @param font  - target font file name from assets/fonts/ directory of a project
     */
    public static void setFontToViews(final Typeface font, final TextView... views) {
        if (font != null)
            for (TextView view : views)
                if (view != null)
                    view.setTypeface(font);
    }

    /**
     * Returns non converted dimension value disregarding any density manipulations or to pixels
     * conversion. So if you set resource as 20sp the 20 will be returned. If you has 20sp for mdpi
     * and 30sp for xhdpi and you run it on xhdpi device 30 will be returned.
     * If you call standard Resources getDimension(dimensResId) it will return a value adjusted
     * respectively to device density, so you will never get 20 for device which is not mdpi.
     * However when you set obtained from getDimension value to a TextView you will be surprised
     * because Android will adjust given value once again. For example: assume you have a dimension
     * <dimen name="text_size_20">20sp</dimen>
     * in your values folder. On hdpi device you will get 30 from getDimension() method which is OK
     * cuz it is auto scaled by Android value regarding to your device density. But when you
     * set this value (30) to a TextView it became 40 due to another scaling made by Android
     * regarding to your device.
     * So use this method to avoid this double scaling feature.
     *
     * @param context
     * @param resId
     * @return
     */
    public static float getTextSizeFromDimens(Context context, int resId) {
        return getTextSizeFromDimens(context.getResources(), resId);
    }

    /**
     * The same method as getTextSizeFromDimens(Context context, int resId) but expects Initializer
     * to be initialized with context (it keeps Application Context)
     *
     * @param resId
     * @return
     */
    public static float getTextSizeFromDimens(int resId) {
        return getTextSizeFromDimens(Initializer.getResources(), resId);
    }

    /**
     * Returns non converted dimension value disregarding any density manipulations or to pixels
     * conversion. So if you set resource as 20sp the 20 will be returned. If you has 20sp for mdpi
     * and 30sp for xhdpi and you run it on xhdpi device 30 will be returned.
     * If you call standard Resources getDimension(dimensResId) it will return a value adjusted
     * respectively to device density, so you will never get 20 for device which is not mdpi.
     * However when you set obtained from getDimension value to a TextView you will be surprised
     * because Android will adjust given value once again. For example: assume you have a dimension
     * <dimen name="text_size_20">20sp</dimen>
     * in your values folder. On hdpi device you will get 30 from getDimension() method which is OK
     * cuz it is auto scaled by Android value regarding to your device density. But when you
     * set this value (30) to a TextView it became 40 due to another scaling made by Android
     * regarding to your device.
     * So use this method to avoid this double scaling feature.
     *
     * @param resources
     * @param resId
     * @return
     */
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

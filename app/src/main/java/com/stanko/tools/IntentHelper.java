package com.stanko.tools;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * by Devlight
 * <p>
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
 */
public class IntentHelper {

    /**
     * Starts Intent to open an external browser with given url
     *
     * @param context
     * @param url     - url to be opened in a browser
     * @return true if Intent starts without any Exception or false otherwise
     */
    public static boolean openUrlInBrowser(final Context context, final String url) {
        boolean isStarted = true;
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        // could be device with no browser installed OR SecurityException
        try {
            context.startActivity(browserIntent);
//        } catch (SecurityException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            isStarted = false;
            e.printStackTrace();
        }
        return isStarted;
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     * <p/>
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     * <p/>
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     *
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        final ResolveInfo serviceInfo = resolveInfo.get(0);
        final String packageName = serviceInfo.serviceInfo.packageName;
        final String className = serviceInfo.serviceInfo.name;
        final ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        final Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    /**
     * Starts EMail intent with filled by given receiver, subject and text
     * It calls corresponding EMailHelper.sendEmail() method
     *
     * @param context
     * @param receivers - an array of email addresses
     * @param subject   - email subject
     * @param text      - the email text/message
     */
    public static void shareByEmail(final Context context,
                                    final String[] receivers,
                                    final String subject,
                                    final String text,
                                    String pickerTitle,
                                    String securityExceptionMessage,
                                    String noAssociatedAppErrorMessage
    ) {
        EMailHelper.sendEmail(context,
                receivers,
                subject,
                text,
                pickerTitle,
                securityExceptionMessage,
                noAssociatedAppErrorMessage);
    }

    /**
     * Starts EMail intent with filled by given subject and text (without receiver email)
     * It calls corresponding EMailHelper.sendEmail() method
     *
     * @param context
     * @param subject                     - email subject
     * @param text                        - email text/message
     * @param pickerTitle                 - custom title to show in app picker
     * @param securityExceptionMessage    - custom message to Toast in case of SEx)
     * @param noAssociatedAppErrorMessage - custom message to Toast in case of no email app installed
     */
    public static void shareByEmail(final Context context,
                                    final String subject,
                                    final String text,
                                    String pickerTitle,
                                    String securityExceptionMessage,
                                    String noAssociatedAppErrorMessage
    ) {
        EMailHelper.sendEmail(context,
                new String[]{},
                subject,
                text,
                pickerTitle,
                securityExceptionMessage,
                noAssociatedAppErrorMessage);
    }

    /**
     * Starts EMail intent with filled by given subject and text (without receiver email)
     * It calls corresponding EMailHelper.sendEmail() method
     *
     * @param context
     * @param subject     - email subject
     * @param text        - email text/message
     * @param pickerTitle - custom title to show in app picker
     */
    public static void shareByEmail(final Context context,
                                    final String subject,
                                    final String text,
                                    String pickerTitle
    ) {
        EMailHelper.sendEmail(context,
                new String[]{},
                subject,
                text,
                pickerTitle,
                null,
                null);
    }

    /**
     * Starts EMail intent with filled by given subject and text (without receiver email)
     * It calls corresponding EMailHelper.sendEmail() method
     *
     * @param context
     * @param subject - email subject
     * @param text    - email text/message
     */
    public static void shareByEmail(final Context context,
                                    final String subject,
                                    final String text
    ) {
        EMailHelper.sendEmail(context,
                new String[]{},
                subject,
                text,
                null,
                null,
                null);
    }

    /**
     * Method to start share text Intent.
     *
     * @param context
     * @param text    - a message to share
     * @return true if Intent starts without any Exception, false otherwise
     */
    public static boolean shareText(final Context context,
                                    final String text) {
        return shareText(context, text, null);
    }

    /**
     * Method to start share text Intent.
     *
     * @param context
     * @param text        - a message to share
     * @param pickerTitle - custom title to show in app picker
     * @return true if Intent starts without any Exception, false otherwise
     */
    public static boolean shareText(final Context context,
                                    final String text,
                                    final String pickerTitle) {
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(text))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        sharingIntent.setType("text/plain");
        return startIntent(context, sharingIntent, pickerTitle);
    }

    /**
     * Method to start share image Intent.
     *
     * @param context
     * @param imageFile - a File pointing to the image to share
     * @return true if Intent starts without any Exception, false otherwise
     */
    public static boolean shareImage(final Context context,
                                     final File imageFile) {
        return shareImage(context, imageFile, null, null);
    }

    /**
     * Method to start share image Intent.
     *
     * @param context
     * @param imageFile    - a File pointing to the image to share
     * @param shareMessage - a message to share with image
     * @param pickerTitle  - custom title to show in app picker
     * @return true if Intent starts without any Exception, false otherwise
     */
    public static boolean shareImage(final Context context,
                                     final File imageFile,
                                     final String shareMessage,
                                     final String pickerTitle) {
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        if (!TextUtils.isEmpty(shareMessage))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        return startIntent(context, sharingIntent, pickerTitle);
    }

    /**
     * Method to start share audio Intent.
     *
     * @param context
     * @param messageFileMp3 - a File pointing to the mp3 to share
     * @return true if Intent starts without any Exception, false otherwise
     */
    public static boolean shareAudio(final Context context,
                                     final File messageFileMp3) {
        return shareAudio(context, messageFileMp3, null, null);
    }

    /**
     * Method to start share audio Intent.
     *
     * @param context
     * @param messageFileMp3 - a File pointing to the mp3 to share
     * @param shareMessage   - a message to share with mp3
     * @param pickerTitle    - custom title to show in app picker
     * @return true if Intent starts without any Exception, false otherwise
     */
    public static boolean shareAudio(final Context context,
                                     final File messageFileMp3,
                                     final String shareMessage,
                                     final String pickerTitle) {
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(messageFileMp3));
        if (!TextUtils.isEmpty(shareMessage))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        sharingIntent.setType("audio/*");
        return startIntent(context, sharingIntent, pickerTitle);
    }

    private static boolean startIntent(final Context context, final Intent sharingIntent, final String pickerTitle) {
        boolean isSharingIntentStarted = true;
        try {
            if (TextUtils.isEmpty(pickerTitle))
                context.startActivity(Intent.createChooser(sharingIntent, "Share using"));
            else
                context.startActivity(Intent.createChooser(sharingIntent, pickerTitle));
        } catch (ActivityNotFoundException e) {
//                Toast.makeText(context, noAssociatedAppErrorMessage, Toast.LENGTH_LONG).show();
            isSharingIntentStarted = false;
        } catch (SecurityException e) {
//                Toast.makeText(context, securityExceptionMessage, Toast.LENGTH_LONG).show();
            isSharingIntentStarted = false;
        }
        return isSharingIntentStarted;
    }

    /**
     * Calls Dialer app with given phone number
     *
     * @param context
     * @param phoneNumber - string with phone number
     * @return true is Intent starts without any Exception, false otherwise
     */
    public static boolean dialPhoneNumber(final Context context, String phoneNumber) {
        Initializer.init(context);
        boolean isStarted = true;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        try {
            phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        intent.setData(Uri.parse("tel:" + phoneNumber));
        // could be device with no browser installed OR SecurityException
        try {
            if (intent.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(intent);
        } catch (Throwable e) {
            isStarted = false;
            e.printStackTrace();
        }
        return isStarted;
    }

    /**
     * Shows map positioned by geoLocation
     *
     * @param context
     * @param geoLocation - uri with geolocation
     * @return true if Intent starts without exceptions, false otherwise
     */
    public static boolean showMap(final Context context, final Uri geoLocation) {
        boolean isStarted = true;
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        // could be device with no browser installed OR SecurityException
        try {
            if (intent.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(intent);
//        } catch (SecurityException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            isStarted = false;
            e.printStackTrace();
        }
        return isStarted;
    }

    /**
     * This method checks if app of given package is installed and if it is so returns an empty
     * Intent targeting this package. You need to add category, action and any other extra data
     * this Intent needs yourself.
     * Note: This method uses Initializer context
     *
     * @return Intent if given package was found and applied or null otherwise
     */
    public static Intent getIntentForPackage(final String targetPackage) {
        return getIntentForPackage(Initializer.getsAppContext(),targetPackage);
    }

    /**
     * This method checks if app of given package is installed and if it is so returns an empty
     * Intent targeting this package. You need to add category, action and any other extra data
     * this Intent needs yourself.
     *
     * @return Intent if given package was found and applied or null otherwise
     */
    public static Intent getIntentForPackage(final Context appContext, final String targetPackage) {
        final Context context = Initializer.getsAppContext();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        int count = 0;
        String intentPackage = null;
        for (ResolveInfo info : pkgAppsList) {
            if (info.activityInfo.packageName.toLowerCase(Locale.US).contains(targetPackage)
                    || info.activityInfo.name.toLowerCase(Locale.US).contains(targetPackage)) {
                intentPackage = info.activityInfo.packageName;
                count++;
            }
        }
        if (count == 1) {
            final Intent intent = new Intent();
            intent.setPackage(intentPackage);
            return intent;
        }
        return null;
    }

}
package com.stanko.tools;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.List;

/**
 * by Devlight
 *
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
 */
public class IntentHelper {

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
     *
     * @param context
     * @param receivers
     * @param subject
     * @param text
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

    public static boolean shareText(final Context context,
                                    final String text) {
        return shareText(context, text, null);
    }

    public static boolean shareText(final Context context,
                                    final String text,
                                    final String pickerTitle)
    {
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(text))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        sharingIntent.setType("text/plain");
        return startIntent(context, sharingIntent, pickerTitle);
    }

    public static boolean shareImage(final Context context,
                                     final File imageFile) {
        return shareImage(context, imageFile, null, null);
    }

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

    public static boolean shareAudio(final Context context,
                                     final File messageFileMp3)
    {
        return shareAudio(context, messageFileMp3, null, null);
    }

    public static boolean shareAudio(final Context context,
                                     final File messageFileMp3,
                                     final String shareMessage,
                                     final String pickerTitle)
    {
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
     * Dials phone number
     * @param context
     * @param phoneNumber
     * @return
     */
    public static boolean dialPhoneNumber(final Context context, final String phoneNumber) {
        boolean isStarted = true;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
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
     * Shows map positioned by geoLocation
     * @param context
     * @param geoLocation
     * @return
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

}
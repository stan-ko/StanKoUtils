package com.stanko.tools;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Created by Stan Koshutsky <stan.koshutsky@gmail.com> on 26.09.2015.
 */

public class SmsMmsHelper {

    public static void sendSMS(Context context,
                               final String phoneNumber,
                               final String text2Send,
                               final int securityExceptionMessageResId,
                               final int noSupportMessageResId)
    {
        sendSMS(context,phoneNumber,text2Send,context.getString(securityExceptionMessageResId),context.getString(noSupportMessageResId));
    }

    public static void sendSMS(Context context,
                               final String phoneNumber,
                               final String text2Send,
                               final String securityExceptionMessage,
                               final String noSupportMessage)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("address", phoneNumber);
        intent.putExtra("sms_body", text2Send);
        intent.putExtra("exit_on_sent", true);
        if (applyKnownPackage(context, intent)){
            try {
                context.startActivity(intent);
            } catch (SecurityException e) {
                Toast.makeText(context, securityExceptionMessage, Toast.LENGTH_LONG).show();
            }
        }
        else if (intent.resolveActivity(context.getPackageManager()) != null)
            try {
                context.startActivity(intent);
            } catch (SecurityException e) {
                Toast.makeText(context, securityExceptionMessage, Toast.LENGTH_LONG).show();
            }
        else
            Toast.makeText(context, noSupportMessage, Toast.LENGTH_SHORT).show();
    }

    public static void sendMMS(Context context,
                               final String phoneNumbers,
                               final String text2Send,
                               final int pickerTitleResId,
                               final int securityExceptionMessageResId,
                               final int noSupportMessageResId,
                               final File file) {
        sendMMS(context, phoneNumbers, text2Send,
                context.getString(pickerTitleResId),
                context.getString(securityExceptionMessageResId),
                context.getString(noSupportMessageResId),
                file);
    }

    public static void sendMMS(Context context,
                               final String phoneNumbers,
                               final String text2Send,
                               final String pickerTitle,
                               final String securityExceptionMessage,
                               final String noSupportMessage,
                               final File file) {
        final Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setData(Uri.parse("smsto:" + phoneNumbers));
        sendIntent.putExtra("address", phoneNumbers);
        sendIntent.putExtra("sms_body", text2Send);
        final Uri uri = Uri.fromFile(file);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("audio/mp3"); //3gp
        //sendIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity"); // not working in most cases
        //sendIntent.setType("vnd.android-dir/mms-sms");

        if (applyKnownPackage(context,sendIntent)){
            try {
                context.startActivity(Intent.createChooser(sendIntent, pickerTitle));
            } catch (SecurityException e) {
                Toast.makeText(context, securityExceptionMessage, Toast.LENGTH_LONG).show();
            }
        }
        else {
            // since no sms-app was found we will try to send using any other app
            sendIntent.putExtra(Intent.EXTRA_TEXT, text2Send);
            try {
                context.startActivity(Intent.createChooser(sendIntent, pickerTitle));
            } catch (SecurityException e) {
                Toast.makeText(context, securityExceptionMessage, Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, noSupportMessage, Toast.LENGTH_LONG).show();
            }
        }

    }


    private static final String[] knownPackages = new String[]{
            "com.textra",
            "com.google.android.apps.messaging",
            "com.jb.gosms",
            "com.p1.chompsms",
            "com.concentriclivers.mms.com.android.mms",
            "com.handcent.app.nextsms",
            "com.alex.mms.com.android.mms",
            "sun.way2sms.hyd.com",
            "com.jb.zerosms",
            "crometh.android.nowsms",
            "com.np.wp7msg",
            "com.klinker.android.evolve_sms",
            "com.mysms.android.sms",
            "com.kksms",
            "com.crazystudio.mms",
            "com.thumbfly.fastestsms",
            "cz.vojtisek.freesmssender",
            "fr.slvn.mms",
            "com.textmeinc.textme",
            "com.mightytext.tablet",
            "com.thinkyeah.message",
            "com.websms.ua",
            "com.asus.message",
            "com.crazystudio.mms7.free",
            "com.gogii.textplus",
            "com.texty.sms",
            "com.moez.qksms",
            "com.thinkleft.eightyeightsms.mms",
            "com.android.mms",
    };

    @SuppressLint("NewApi")
    private static boolean applyKnownPackage(final Context context, final Intent sendIntent){
        // At least KitKat. Need to change the build to API 19
        // still no package of sms-app
        if (DeviceInfo.hasAPI(19)) {
            final String smsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
            // Can be null in case that there is no default, then the user
            // would be able to choose any app that support this intent.
            if (!TextUtils.isEmpty(smsPackageName)){
                sendIntent.setPackage(smsPackageName);
                return true;
            }
        }

        final List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(sendIntent, 0);
        if (resInfo.size() == 0)
            return false;
        if (resInfo.size()==1){
            sendIntent.setPackage(resInfo.get(0).activityInfo.packageName);
            return true;
        }
        // found some apps: lets check these app looking for known knownPackages
        for (ResolveInfo info : resInfo) {
            for (String aPackage : knownPackages) {
                if (info.activityInfo.packageName.toLowerCase(Locale.US).contains(aPackage)
                        || info.activityInfo.name.toLowerCase(Locale.US).contains(aPackage)) {
                    // found listed in known knownPackages app! Will use it
                    sendIntent.setPackage(info.activityInfo.packageName);
                    return true;
                }
            }
        }
        return true;
    }
}
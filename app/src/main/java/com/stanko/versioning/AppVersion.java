package com.stanko.versioning;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.stanko.tools.Log;

public class AppVersion {

    public static int getVersionCode(final Context context) {
        int version = 0;
        try {
            final String packageName = context.getPackageName();
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            version = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
        return version;
    }

    public static String getVersionName(final Context context) {
        String version = "";
        try {
            final String packageName = context.getPackageName();
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
        return version;
    }

    public static AppVersionInfo getVersionInfo(final Context context) {
        return new AppVersionInfo(context);
    }

}

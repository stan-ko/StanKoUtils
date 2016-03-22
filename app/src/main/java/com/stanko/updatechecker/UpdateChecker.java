package com.stanko.updatechecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.stanko.R;
import com.stanko.tools.OrientationUtils;
import com.stanko.tools.SharedPrefsHelper;
import com.stanko.versioning.DefaultArtifactVersion;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/*
 * <p/>
 * Authors:
 * Stan Koshutsky<Stan.Koshutsky@gmail.com>
 */

public class UpdateChecker implements Runnable {

    /**
     * The Context from which the updater is called
     */
    private final Context mContext;

    /**
     * The package name of your app
     */
    private final String mPackageName;
    /**
     * The current version of your app
     */
    private final String mCurrentVersion;
    private final int mCurrentVersionCode;
    /**
     * Each time you enter in an Activity which for example called: <b>new UpdateChecker(this, new Handler()).start();</b>
     * this is the minimum time which has to pass between an automatic verification of an update and the next automatic verification.
     */
    private final long TIME_TO_RETRY_CHECK;

    public static final long SEC = 1000;
    public static final long MIN = 60 * SEC;
    public static final long HOUR = 60 * MIN;
    public static final long DAY = 24 * HOUR;

    private final Object mEventToPost;

    /**
     * Represents if a new update exists or not on the Google Play Store
     */
    private static boolean isUpdateAvailable = false;

    /**
     * Updater Runnable constructor
     *
     * @param context     the activity from which the updater is called
     * @param timeToRetry time in millis which represents the time after which the runnable called with force = false have to retry to check if an update exists
     */
    public UpdateChecker(final Context context, final long timeToRetry) {
        this(context, timeToRetry, new NewVersionAvailableEvent());
    }

    public UpdateChecker(final Context context, final long timeToRetry, final Object eventToPost) {
        mContext = context.getApplicationContext();
        mPackageName = context.getPackageName();
        String currentVersion = null;
        int currentVersionCode = 0;
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersion = packageInfo.versionName;
            currentVersionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
        }
        this.mCurrentVersion = currentVersion;
        mCurrentVersionCode = currentVersionCode;
        this.TIME_TO_RETRY_CHECK = timeToRetry;
        mEventToPost = eventToPost;
    }

    /**
     * Updater Runnable constructor, when called in automatic way it runs once a day
     *
     * @param context context
     */
    public UpdateChecker(final Context context) {
        this(context.getApplicationContext(), DAY);
    }


    public UpdateChecker(final Context context, final Object eventToPost) {
        this(context.getApplicationContext(), DAY, eventToPost);
    }

//    /**
//     * If true it means that the user has explicitly asked to verify if a new update exists so the runnable will show a dialog even if
//     * the app is already updated (otherwise the user couldn't have a feedback from his explicit request).
//     * If false it means that verification is started automatically (the user don't asked explicitly to verify), so a dialog is shown to the user
//     * only if an update really exists.
//     *
//     * @param force if true it forces a dialog visualization (even if already updated)
//     * @return an updater
//     */

    public void check() {
        if (isUpdateAvailable) {
            Log.i(UpdateChecker.class.getSimpleName(), "check() isUpdateAvailable: " + isUpdateAvailable);
            EventBus.getDefault().postSticky(mEventToPost);
        } else {
            new Thread(this).start();
        }
    }

    /**
     * Runs the asynchronous web  call and shows on the UI the Dialogs if required
     */
    @Override
    public void run() {
        synchronized (mEventToPost) {
            // Extract from the Internet if an update is needed or not
            if (!isUpdateAvailable)
                isUpdateAvailable = isUpdateAvailable();
        }
        if (isUpdateAvailable) {
            EventBus.getDefault().postSticky(mEventToPost);
        }
    }

    /**
     * Check if you are updated
     *
     * @return true if an update is needed false otherwise
     */
    private boolean isUpdateAvailable() {

        // Check if there is really an update on the Google Play Store
        final Boolean isUpdateAvailableWeb = isUpdateAvailableWeb();
        if (isUpdateAvailableWeb != null) {
            // if successfully checked the update
            setLastTimeUpdateChecked(mContext);

            return isUpdateAvailableWeb;
        } else
            return false;
    }

    /**
     * Check if the Google Play version of the app match or less the current version installed
     *
     * @return true if an update is required, false otherwise
     */
    private Boolean isUpdateAvailableWeb() {
        String webVersion = null, webVersionInt = null;
        try {
            final Document doc =
                    Jsoup.connect("https://play.google.com/store/apps/details?id=" + mPackageName + "&hl=en")
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get();

            if (doc != null) {
                webVersion = doc
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();

                //<button class="dropdown-child" data-dropdown-value="83" tabindex="0">Latest Version</button>
                webVersionInt = doc
                        .select("button.dropdown-child")
                        .select("[data-dropdown-value]")
                        .select(":contains(Latest Version)")
                        .first()
                        .attr("data-dropdown-value");
//                if (TextUtils.isDigitsOnly(webVersionInt)){
                Log.i(UpdateChecker.class.getSimpleName(), "Version Code Check, web: " + webVersionInt + " vs current: " + mCurrentVersionCode);
//                }
//                for (Element element : buttons) {
//                    Log.i("UpdateChecker", "data-dropdown-value: " + element.attr("data-dropdown-value"));
//                }


//            webVersion  = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mPackageName + "&hl=en")
//                    .timeout(30000)
//                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
//                    .referrer("http://www.google.com")
//                    .get()
//                    .select("div[itemprop=softwareVersion]")
//                    .first()
//                    .ownText();
                Log.i(UpdateChecker.class.getSimpleName(), "isUpdateAvailableWeb() webVersion: " + webVersion);
            }
        } catch (Exception e) {
            Log.i(UpdateChecker.class.getSimpleName(), "isUpdateAvailableWeb() Exception: " + e.getMessage());
            return null;
        }
        if (TextUtils.isEmpty(webVersionInt) && TextUtils.isEmpty(webVersion))
            return null;
        else {
            // store last update time
            setLastTimeUpdateCheckedVersion(mContext, webVersion);
            if (!TextUtils.isEmpty(webVersionInt) && TextUtils.isDigitsOnly(webVersionInt)) {
                int weVersionCode = 0;
                try {
                    weVersionCode = Integer.valueOf(webVersionInt);
                    return weVersionCode > mCurrentVersionCode;
                } catch (NumberFormatException e) {
                }
            }
            return isNewerVersionAvailable(mCurrentVersion, webVersion);
        }
    }

    /**
     * Builds and shows the Dialog in case it is not updated
     */
    public static AlertDialog.Builder showUpdateAvailableDialog(final Activity activity) {
        return showUpdateAvailableDialog(activity,
                R.string.you_are_not_updated_title,
                R.string.you_are_not_updated_message,
                R.string.yes,
                R.string.no);
    }

    /**
     * Builds and shows the Dialog in case it is not updated
     */
    public static AlertDialog.Builder showUpdateAvailableDialog(final Activity activity,
                                                                int titleResId,
                                                                int messageResId,
                                                                int labelYesResId,
                                                                int labelNoResId) {

        return showUpdateAvailableDialog(activity,
                activity.getString(titleResId),
                activity.getString(messageResId),
                activity.getString(labelYesResId),
                activity.getString(labelNoResId));
    }

    /**
     * Builds and shows the Dialog in case it is not updated
     */
    public static AlertDialog.Builder showUpdateAvailableDialog(final Activity activity,
                                                                final String title,
                                                                final String message,
                                                                final String labelYes,
                                                                final String labelNo) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
//        alertDialogBuilder.setIcon(getCloudDrawable());
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton(labelNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                OrientationUtils.unlockOrientation(activity);
            }
        });
        alertDialogBuilder.setPositiveButton(labelYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
                dialog.cancel();
                OrientationUtils.unlockOrientation(activity);
            }
        });
        alertDialogBuilder.show();
        return alertDialogBuilder;
    }


    /**
     * Used for comparing different versions of software
     *
     * @param localVersion  the version name of the app installed on the system
     * @param webVersion the version name of the app released on the Google Play
     * @return true if a the online_version_string is greater than the local_version_string
     */
    private static boolean isNewerVersionAvailable(final String localVersion, final String webVersion) {
        final DefaultArtifactVersion dafLocalVersion = new DefaultArtifactVersion(localVersion);
        final DefaultArtifactVersion dafWebVersion = new DefaultArtifactVersion(webVersion);
        return !TextUtils.isEmpty(localVersion)
                && !TextUtils.isEmpty(webVersion)
                && dafLocalVersion.compareTo(dafWebVersion) == -1;
    }

    /**
     * @param context
     * @return the value of preference which represents the last time you verify if an update exists
     */
    private static long getLastTimeUpdateChecked(Context context) {
        return SharedPrefsHelper.getLong(context, getLastUpdateKey(context), 0L);
    }

    /**
     * Sets the value of preference which represents the last time you verify if an update exists = the currentTimeMillis in which that function is called
     *
     * @param context
     */
    private static void setLastTimeUpdateChecked(Context context) {
        SharedPrefsHelper.put(context, getLastUpdateKey(context), System.currentTimeMillis());
    }

    // version
    private static String getLastTimeUpdateCheckedVersion(Context context) {
        return SharedPrefsHelper.getString(context, getLastUpdateVersionKey(context), null);
    }

    private static void setLastTimeUpdateCheckedVersion(Context context, final String version) {
        SharedPrefsHelper.put(context, getLastUpdateVersionKey(context), version);
    }

    /**
     * @param context
     * @return the key String of the Last Update Preference
     */
    private static String getLastUpdateKey(Context context) {
        return "LastTimeUpdateChecked_" + context.getPackageName();
    }

    private static String getLastUpdateVersionKey(Context context) {
        return "LastTimeUpdateCheckedVersion_" + context.getPackageName();
    }

}
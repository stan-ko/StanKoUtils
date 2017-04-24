/*
 * Copyright (C) 2014 Daniele Maddaluno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stanko.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Static methods related to device orientation.
 */
public class OrientationUtils {

    /**
     * Locks the device window in landscape mode.
     *
     * @param activity
     */
    public static void lockOrientationLandscape(final Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * Locks the device window in portrait mode.
     *
     * @param activity
     */
    public static void lockOrientationPortrait(final Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Locks the device window in actual (current) screen mode.
     *
     * @param activity
     */
    public static void lockOrientation(final Activity activity) {
        final int orientation = activity.getResources().getConfiguration().orientation;
        final int rotation = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
        }
    }

    /**
     * Unlocks the device window in user defined screen mode.
     *
     * @param activity
     */
    public static void unlockOrientation(final Activity activity) {
        activity.setRequestedOrientation(getManifestOrientation(activity));
    }

    /**
     * Returns activity orientation declared in Manifest
     *
     * @param activity
     * @return integer, see constant value of orientations on http://developer.android.com/reference/android/R.attr.html#screenOrientation
     */
    public static int getManifestOrientation(final Activity activity) {
        try {
            ActivityInfo app = activity
                    .getPackageManager()
                    .getActivityInfo(activity.getComponentName(), PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            return app.screenOrientation;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public static boolean isCurrentOrientationPortrait(final Context activityContext) {
        final int orientation = activityContext.getResources().getConfiguration().orientation;
        final int rotation = ((WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false;
            }
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false;
            }
        }
        return false;
    }

    public static boolean isCurrentOrientationLandscape(final Context activityContext) {
        return !isCurrentOrientationPortrait(activityContext);
    }

    public static int getCurrentOrientationScreenHeight() {
        return DeviceInfo.getDisplaySize().x;
    }

    public static int getCurrentOrientationScreenWidth() {
        return DeviceInfo.getDisplaySize().y;
    }

}

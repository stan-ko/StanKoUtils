package com.stanko.tools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.stanko.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

/*
 * Created by Stan Koshutsky <Stan.Koshutsky@gmail.com>
 * This class requires INITIALIZATION with Application Context to work properly
 */
public class DeviceInfo {

    private static boolean sIsInitialized;
    private static Context sAppContext;

    // display section
    private static DisplayMetrics sDisplayMetrics = new DisplayMetrics();
    private static int sDisplayDensity;
    private static int sDisplayHeight;
    private static int sDisplayWidth;
    private static int sDisplayPortraitHeight;
    private static int sDisplayPortraitWidth;
    private static int sScreenSize;
    private static int sScreenInches;
    private static float sScreenInchesByMetrics;
    private static float sScreenInchesByConfig;
    private static float sConfigurationRatio;
    private static boolean sHasPermanentMenuKeys;
    private static boolean sHasNavigationBar;
    private static int sNavigationBarHeight;
    private static int sStatusBarHeight;

    // device info section
    public static final int sAPILevel = Build.VERSION.SDK_INT;
    public static final String sDeviceName = android.os.Build.DEVICE.toLowerCase(Locale.US);
    public static final String sDeviceBrand = android.os.Build.BRAND.toLowerCase(Locale.US);
    public static final String sDeviceModel = android.os.Build.MODEL.toLowerCase(Locale.US);
    public static final String sDeviceManufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.US);
    public static final String sDeviceProduct = android.os.Build.PRODUCT.toLowerCase(Locale.US);

    private static String sDeviceARM = Build.CPU_ABI;
    private static String[] sDeviceARMs = new String[]{Build.CPU_ABI, Build.CPU_ABI2};

    private static boolean sHasTelephony;
    private static int sTelephonyType;

    public static DisplayMetrics getDisplayMetrics() {
        initOnDemand();
        return sDisplayMetrics;
    }

    public static int getDisplayDensity() {
        initOnDemand();
        return sDisplayDensity;
    }

    public static int getDisplayHeight() {
        initOnDemand();
        return sDisplayHeight;
    }

    public static int getDisplayWidth() {
        initOnDemand();
        return sDisplayWidth;
    }

    public static int getDisplayPortraitHeight() {
        initOnDemand();
        return sDisplayPortraitHeight;
    }

    public static int getDisplayPortraitWidth() {
        initOnDemand();
        return sDisplayPortraitWidth;
    }

    public static int getScreenSize() {
        initOnDemand();
        return sScreenSize;
    }

    public static int getScreenInches() {
        initOnDemand();
        return sScreenInches;
    }

    public static float getScreenInchesByMetrics() {
        initOnDemand();
        return sScreenInchesByMetrics;
    }

    public static float getScreenInchesByConfig() {
        initOnDemand();
        return sScreenInchesByConfig;
    }

    public static float getsConfigurationRatio() {
        initOnDemand();
        return sConfigurationRatio;
    }

    public static boolean issHasPermanentMenuKeys() {
        initOnDemand();
        return sHasPermanentMenuKeys;
    }

    public static boolean issHasNavigationBar() {
        initOnDemand();
        return sHasNavigationBar;
    }

    public static int getNavigationBarHeight() {
        initOnDemand();
        return sNavigationBarHeight;
    }

    public static int getStatusBarHeight() {
        initOnDemand();
        return sStatusBarHeight;
    }

    public static int getAPILevel() {
        return sAPILevel;
    }

    public static String getDeviceBrand() {
        return sDeviceBrand;
    }

    public static String getDeviceModel() {
        return sDeviceModel;
    }

    public static String getDeviceManufacturer() {
        return sDeviceManufacturer;
    }

    public static String getDeviceProduct() {
        return sDeviceProduct;
    }

    public static String getDeviceName() {
        return sDeviceName;
    }

    public static String getDeviceARM() {
        initOnDemand();
        return sDeviceARM;
    }

    public static String[] getDeviceARMs() {
        initOnDemand();
        return sDeviceARMs;
    }

    public static boolean isHasTelephony() {
        initOnDemand();
        return sHasTelephony;
    }

    public static int getTelephonyType() {
        initOnDemand();
        return sTelephonyType;
    }

    /**
     * To determine device portrait width
     *
     * @return int  smallest device screen side size
     */
    public static int getSmallestScreenSideSize() {
        return Math.min(sDisplayHeight, sDisplayWidth);
    }

    /**
     * To determine device portrait height
     *
     * @return int  greatest device screen side size
     */
    public static int getBiggestScreenSideSize() {
        return Math.max(sDisplayHeight, sDisplayWidth);
    }


    /**
     * if given context is null a NPE will be thrown
     *
     * @return boolean  true if initialized successfully
     */
    @SuppressLint("NewApi")
    public static boolean init(final Context context) {

        if (sIsInitialized && context != null)
            return true;

        sAppContext = context.getApplicationContext(); // to be sure it is an Application Context
        final Resources resources = sAppContext.getResources();
        final Display display = ((WindowManager) sAppContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (sAPILevel < 17) {
            display.getMetrics(sDisplayMetrics);
        } else /*if (hasAPI(17))*/ {
            display.getRealMetrics(sDisplayMetrics);
        }

        // initial display size values
        DisplayMetrics resourceDisplayMetrics = sAppContext.getResources().getDisplayMetrics();
        final int metricsDisplayHeight = resourceDisplayMetrics.heightPixels;
        final int metricsDisplayWidth = resourceDisplayMetrics.widthPixels;
        int realDisplayHeight = metricsDisplayHeight, realDisplayWidth = metricsDisplayWidth;
        final Point sizePoint = getDisplaySize();
        if (sizePoint.x > 0 && sizePoint.y > 0) {
            realDisplayWidth = sizePoint.x;
            realDisplayHeight = sizePoint.y;
        }

        sHasPermanentMenuKeys = metricsDisplayHeight == realDisplayHeight;
        sDisplayDensity = resourceDisplayMetrics.densityDpi;
        sDisplayHeight = realDisplayHeight;
        sDisplayWidth = realDisplayWidth;
        sDisplayPortraitHeight = getBiggestScreenSideSize();
        sDisplayPortraitWidth = getSmallestScreenSideSize();

        // http://stackoverflow.com/a/28983720/1811719
        try {
            final boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            final boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
            sHasNavigationBar = !(hasBackKey && hasHomeKey);
            if (sHasNavigationBar) {
                //The device has a navigation bar
                final int orientation = resources.getConfiguration().orientation;
                int resourceId;
                if (isTablet()) {
                    resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
                } else {
                    resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
                }
                if (resourceId > 0) {
                    sNavigationBarHeight = resources.getDimensionPixelSize(resourceId);
                }
            }
        } catch (Exception e) {
            Log.e(e);
            /*
             * Caused by java.lang.NullPointerException: Attempt to invoke interface method 'boolean android.hardware.input.IInputManager.hasKeys(int, int, int[], boolean[])' on a null object reference
             * at android.hardware.input.InputManager.deviceHasKeys(InputManager.java:704)
             * at android.hardware.input.InputManager.deviceHasKeys(InputManager.java:685)
             * at android.view.KeyCharacterMap.deviceHasKey(KeyCharacterMap.java:697)
             */
        }

        // Status Bar Height
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            sStatusBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            sStatusBarHeight = (int) Math.ceil(24 * sDisplayMetrics.density);
        }
//        else {
//            final Rect rect = new Rect();
//            sAppContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//            sStatusBarHeight = rect.top;
//        }

        final double xDensity = Math.pow(realDisplayWidth / sDisplayMetrics.xdpi, 2);
        final double yDensity = Math.pow(realDisplayHeight / sDisplayMetrics.ydpi, 2);
        sScreenInchesByMetrics = Math.round(Math.sqrt(xDensity + yDensity) * 10f) / 10f;
        sScreenInches = Math.round(sScreenInchesByMetrics);

        Log.i(String.format("Model: %s, Manufacturer: %s Product: %s Name: %s", sDeviceModel, sDeviceManufacturer, sDeviceProduct, sDeviceName));
        Log.i("Device platform: ABI: " + Build.CPU_ABI + " ABI2: " + Build.CPU_ABI2);

        if (hasAPI(21)) {
            if (Build.SUPPORTED_ABIS != null)
                sDeviceARMs = Build.SUPPORTED_ABIS;
            if (sDeviceARMs.length > 0)
                sDeviceARM = Build.SUPPORTED_ABIS[0];
        }

        final Configuration conf = sAppContext.getResources().getConfiguration();
        Log.i(String.format("Screen conf.screenHeightDp: %s, conf.screenWidthDp: %s", conf.screenHeightDp, conf.screenWidthDp));
        sScreenInchesByConfig = Math.round(Math.sqrt(conf.screenHeightDp * conf.screenHeightDp + conf.screenWidthDp * conf.screenWidthDp) * 10f) / 10f;

        final int screenLayout = conf.screenLayout;

        sScreenSize = screenLayout & 15;

        switch (sScreenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                sConfigurationRatio = .75f;
                break;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                sConfigurationRatio = 1f;
                break;

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                sConfigurationRatio = 1.5f;
                break;

            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                sConfigurationRatio = 2f;
                break;

            default:
                // undefined
                break;
        }

        Log.i(String.format(Locale.US, "Display: Density %d, Width: %d Height: %d sConfigurationRatio: %f", sDisplayDensity, sDisplayWidth, sDisplayHeight, sConfigurationRatio));
        Log.i(String.format(Locale.US, "Display: DensityDpi %d, Density %f, Width: %d Height: %d", sDisplayMetrics.densityDpi, sDisplayMetrics.density, sDisplayMetrics.widthPixels, sDisplayMetrics.heightPixels));

        checkHasTelephony(sAppContext);

        sIsInitialized = true;

        return true;
    }

    private static void initOnDemand() {
        // init on demand
        if (sAppContext == null && Initializer.getsAppContext() != null) {
            init(Initializer.getsAppContext());
        }
    }

//    private static boolean deviceNotHasSoftNavigationBar() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            final Point realSize = new Point();
//            final Point visibleSize = new Point();
//            mMainActivity.getWindowManager().getDefaultDisplay().getRealSize(realSize);
//            mMainActivity.getWindowManager().getDefaultDisplay().getSize(visibleSize);
//            return (realSize.y == visibleSize.y);
//        }
//        return false;
//    }

    public static int getStatusBarHeight(final Activity activity) {
        final Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    public static int getStatusBarHeight(final Context context) {
        if (!sIsInitialized)
            init(context);
        return sStatusBarHeight;
    }

    public static int getNavigationBarHeight(final Context context) {
        if (!sIsInitialized)
            init(context);
        return sNavigationBarHeight;
    }

    @SuppressLint("NewApi")
    public static Point getDisplaySize() {
        final Resources resources = sAppContext.getResources();
        final Display display = ((WindowManager) sAppContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (sAPILevel < 17) {
            display.getMetrics(sDisplayMetrics);
        } else /*if (hasAPI(17))*/ {
            display.getRealMetrics(sDisplayMetrics);
        }

        // initial display size values
        DisplayMetrics resourceDisplayMetrics = sAppContext.getResources().getDisplayMetrics();
        final int metricsDisplayHeight = resourceDisplayMetrics.heightPixels;
        final int metricsDisplayWidth = resourceDisplayMetrics.widthPixels;
        int realDisplayHeight = metricsDisplayHeight, realDisplayWidth = metricsDisplayWidth;
        final Point sizePoint = new Point(0, 0);
        if (sAPILevel > 16) {
            boolean tryToUseReflection = false;
            try {
                display.getRealSize(sizePoint);
            } catch (Exception ignored) {
                tryToUseReflection = true;
            }
            if (tryToUseReflection) {
                // includes window decorations (statusbar bar/menu bar)
                try {
                    Display.class.getMethod("getRealSize", Point.class).invoke(display, sizePoint);
                } catch (Exception ignored) {
                }
            }
        }
        // also for API lower than 17
        if (sizePoint.x == 0 || sizePoint.y == 0) {
            if (sAPILevel < 14) {
                sizePoint.set(sDisplayMetrics.widthPixels, sDisplayMetrics.heightPixels);
            } else /*if (sAPILevel > 13 && sAPILevel < 17)*/ {
                // includes window decorations (statusbar bar/menu bar) 14,15,16 api levels
                // or if API 17 does not have getRealSize() method for some reason (impossible?)
                try {
                    sizePoint.set((int) Display.class.getMethod("getRawWidth").invoke(display),
                            (int) Display.class.getMethod("getRawHeight").invoke(display));
                } catch (Exception ignored) {
                }
            }
        }
        if (sizePoint.x > 0 && sizePoint.y > 0) {
            realDisplayWidth = sizePoint.x;
            realDisplayHeight = sizePoint.y;
        }
        return sizePoint;
    }

    public static int getCurrentOrientationDisplayHeight(){
        return getDisplaySize().x;
    }

    public static int getCurrentOrientationDisplayWidth(){
        return getDisplaySize().y;
    }

    public static boolean isTablet() {
        return isTabletByResources();
    }

    public static boolean isTabletByResources() {
        initOnDemand();
        final boolean isTabletByResources = sAppContext.getResources().getBoolean(R.bool.isTablet);
        Log.i("isTabletByResources: " + isTabletByResources);
        return isTabletByResources;
    }

    /**
     * Tries to determine device is Tablet or SmartPhone by its screen
     *
     * @return
     */
    public static Boolean isTabletByScreen() {

        if (!sIsInitialized)
            return null;

        boolean byScreen = sScreenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
                & sScreenInches >= 7
                & (sDisplayDensity == DisplayMetrics.DENSITY_DEFAULT
                || sDisplayDensity == DisplayMetrics.DENSITY_HIGH
                || sDisplayDensity == DisplayMetrics.DENSITY_MEDIUM
                || sDisplayDensity == 213 //DisplayMetrics.DENSITY_TV
                || sDisplayDensity == 320 /*DisplayMetrics.DENSITY_XHIGH*/
        );
        // If >Large, checks if the Generalized Density is at least MDPI
        // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160,
        // DENSITY_TV=213, DENSITY_XHIGH=320
        Log.i("isTabletByScreen: " + byScreen + " sScreenSize: " + sScreenSize);
        return byScreen;
    }

    /**
     * Converts pixels to dp based on device density
     *
     * @param px
     * @return
     */
    public static float px2dp(float px) {
        if (sIsInitialized)
            return (float) ((px / sDisplayMetrics.density) + 0.5);
            //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, sDisplayMetrics);
        else {
            Log.w("Class is not initialized!!! Method  px2dp() returns 0");
            return 0;
        }
    }

    /**
     * Converts dps to pixels based on device density
     *
     * @param dp
     * @return
     */
    public static float dp2px(float dp) {
        if (sIsInitialized)
            return (float) ((dp * sDisplayMetrics.density) + 0.5);
            //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, sDisplayMetrics);
        else {
            Log.w("Class is not initialized!!! Method  dp2px() returns 0");
            return 0;
        }
    }

    /**
     * Converts sp (Font size) to pixels
     *
     * @param sp
     * @return
     */
    public static float sp2px(float sp) {
        if (sIsInitialized)
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, sDisplayMetrics);
        else {
            Log.w("Class is not initialized!!! Method  dp2px() returns 0");
            return 0;
        }
    }


    public static boolean isScreenSizeSmall() {
        return sScreenSize == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    public static boolean isScreenSizeNormal() {
        return sScreenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public static boolean isScreenSizeLarge() {
        return sScreenSize == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isScreenSizeXLarge() {
        return sScreenSize == 4; //Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public static float getConfigurationRatio() {
        return sConfigurationRatio;
    }

    public static boolean hasTelephony() {
        return sHasTelephony;
    }

    public static void checkHasTelephony(Context context) {
        // sIsInitialized==true means this method was already executed
//        if (!sIsInitialized) {
        TelephonyManager telephonyManager;
        try { // for the firecase
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Throwable e) {
            sHasTelephony = false;
            return;
        }
        sTelephonyType = telephonyManager == null ? TelephonyManager.PHONE_TYPE_NONE : telephonyManager.getPhoneType();
        if (sTelephonyType == TelephonyManager.PHONE_TYPE_NONE) {
            sHasTelephony = false;
            if (telephonyManager == null)
                Log.i("Has NO telephony: TelephonyManager is null");
            else
                Log.i("Has NO telephony: TelephonyManager.getPhoneType == PHONE_TYPE_NONE");
        } else {
            sHasTelephony = true;
            //Phone Type
            // The getPhoneType() returns the device type. This method returns one of the following values:
            switch (sTelephonyType) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    Log.i("Has GSM Telephony!");
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    Log.i("Has CDMA Telephony!");
                    break;
                case TelephonyManager.PHONE_TYPE_SIP:
                    Log.i("Has SIP Telephony!");
                    break;
                default:
                    Log.i("Has UNKNOWN TYPE Telephony: " + sTelephonyType);
                    break;
            }
        }
//        }
    }

    @SuppressLint("NewApi")
    public static Boolean hasCamera() {
        initOnDemand();
        final PackageManager pm = sAppContext.getPackageManager();
        boolean hasCameraByPM;
        if (pm == null)
            return null;
        else
            hasCameraByPM = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                    || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        if (hasCameraByPM) {
            if (hasAPI(21))
                return hasCameraApi2();
            else
                return Camera.getNumberOfCameras() > 0;
        } else
            return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCameraApi2() {
        initOnDemand();
        try {
            final android.hardware.camera2.CameraManager cameraManager = (android.hardware.camera2.CameraManager) sAppContext.getSystemService(Context.CAMERA_SERVICE);
            final String[] cameraIdList = cameraManager.getCameraIdList();
            return cameraIdList != null && cameraIdList.length > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean hasBackCamera() {
        final Boolean hasCamera = hasCamera();
        if (hasCamera == null)
            return null;
        else if (!hasCamera)
            return false;

        int backCameraId = -1;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backCameraId = i;
                break;
            }
        }
        return backCameraId > -1;
    }


    public static Boolean hasFlash() {
        initOnDemand();
        final PackageManager pm = sAppContext.getPackageManager();
        if (pm == null)
            return null;
        else
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    // unique key for sharedprefs to store generated UUID
    private static final String PREFS_DEVICE_INFO_KEY = "DeviceInfo";
    private static final String PREFS_DEVICE_UUID_KEY = Hash.getMD5("DeviceInfoGeneratedUUID");
    private static String uuid;

    /**
     * Returns a unique String UUID for the current android device.
     * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
     *
     * @return an UUID that may be used to uniquely identify your device for most purposes.
     */
    public static synchronized String getDeviceUuid() {
        initOnDemand();
        return getDeviceUuid(sAppContext);
    }

    /**
     * Returns a unique String UUID for the current android device.
     * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
     *
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an UUID that may be used to uniquely identify your device for most purposes.
     */
    public static synchronized String getDeviceUuid(Context context) {
        // 1st start it'd b null
        if (uuid != null)
            return uuid;

        // lets try to restore DeviceUuid from SharedPrefs
        final SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_DEVICE_INFO_KEY, Context.MODE_PRIVATE);
        final String id = prefs.getString(PREFS_DEVICE_UUID_KEY, null);

        if (!TextUtils.isEmpty(id))
            // Use the DeviceUuid previously computed and stored in SharedPrefs
            uuid = id;
        else {
            uuid = UUID.randomUUID().toString();
            // Store the value in SharedPrefs for further usage
            prefs.edit().putString(PREFS_DEVICE_UUID_KEY, uuid).apply();
        }

        return uuid;
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or Secure.ANDROID_ID
     *
     * @return a ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceId() {
        initOnDemand();
        return getDeviceId(sAppContext);
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or Secure.ANDROID_ID
     *
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceId(Context context) {

        String deviceId = getDeviceIMEI(context);

        if (TextUtils.isEmpty(deviceId) || deviceId.length() < 8)
            deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        return deviceId;
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or null
     *
     * @return a ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceIMEI() {
        initOnDemand();
        return getDeviceIMEI(sAppContext);
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or null
     *
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceIMEI(Context context) {

        if (!sIsInitialized)
            checkHasTelephony(context);

        String deviceId = null;

        if (sHasTelephony) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null)
                deviceId = telephonyManager.getDeviceId();
        }

        return deviceId;
    }

    /**
     * Returns common and safe image maximum side side according to device display density
     *
     * @return
     */
    public static int getDeviceMaxSideSizeByDensity() {
        int maxSideSize = 128;
        switch (sDisplayDensity) {
            case 120:
                maxSideSize = 64;
                break;
            case 160:
                maxSideSize = 96;
                break;
            case 240:
                maxSideSize = 128;
                break;
            case 320:
                maxSideSize = 256;
                break;
            default:
                maxSideSize = 512;
                break;
        }
        return maxSideSize;
    }

    private static final String MANUFACTURER_SAMSUNG = "samsung";
    private static final String DEVICE_ID_GALAXY_S = "gt-i9000";
    private static final String DEVICE_ID_GALAXY_S_PLUS = "gt-i9001";
    private static final String DEVICE_ID_GALAXY_S_CRAFT = "gt-i9002";
    private static final String DEVICE_ID_GALAXY_SL = "gt-i9003";
    private static final String DEVICE_ID_GALAXY_S_II = "gt-i9004";
    private static final String DEVICE_ID_GALAXY_S_II_PLUS = "gt-i9005";
    //    private static final String DEVICE_ID_GALAXY_S_HZ1 = "gt-i9006";
    private static final String DEVICE_ID_GALAXY_S_ADVANCE = "gt-i9007";
    //    private static final String DEVICE_ID_GALAXY_S_HZ2 = "gt-i9008";
    private static final String DEVICE_ID_GALAXY_S_III = "gt-i9009";
    private static final String DEVICE_ID_GALAXY_S_ARMANI = "gt-i9010";
    private static final String DEVICE_ID_GALAXY_S_GRAND = "gt-i9082";
    private static final String DEVICE_ID_CAPTIVATE = "sgh-i897";
    private static final String DEVICE_ID_VIBRANT = "sgh-t959";
    private static final String DEVICE_ID_FASCINATE = "sch-i500";
    private static final String DEVICE_ID_EPIC = "sph-d700";
    private static final String DEVICE_ID_MESMERIZE = "sch-i500";
    private static final String DEVICE_ID_GALAXY_PFX = "gt-i90";

    private static Boolean isSamsungGalaxyShit;

    /**
     * Tries to figure out if the current phone is Galaxy S based, since it has
     * recording issues This is pretty nasty since it checks using string matching
     */
    public static boolean isSamsungGalaxyShit() {

        if (isSamsungGalaxyShit != null)
            return isSamsungGalaxyShit;

        if (!TextUtils.equals(sDeviceManufacturer, MANUFACTURER_SAMSUNG)) {
            isSamsungGalaxyShit = false;
            return false;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S)) {
            Log.i("Samsung Galaxy S detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_PLUS) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_PLUS)) {
            Log.i("Samsung Galaxy S+ detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_CRAFT) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_CRAFT)) {
            Log.i("Samsung Galaxy S Craftman detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_SL) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_SL)) {
            Log.i("Samsung Galaxy SL detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_II) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_II)) {
            Log.i("Samsung Galaxy S II detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_II_PLUS) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_II_PLUS)) {
            Log.i("Samsung Galaxy S II+ detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_ADVANCE) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_ADVANCE)) {
            Log.i("Samsung Galaxy S Advance detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_III) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_III)) {
            Log.i("Samsung Galaxy S III detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_GRAND) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_GRAND)) {
            Log.i("Samsung Galaxy Grand detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_GALAXY_S_ARMANI) || TextUtils.equals(sDeviceModel, DEVICE_ID_GALAXY_S_ARMANI)) {
            Log.i("Samsung Galaxy S Giorgio Armani detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (String.valueOf(sDeviceName).contains("DEVICE_ID_GALAXY_PFX") || String.valueOf(sDeviceModel).contains(DEVICE_ID_GALAXY_PFX)) {
            Log.i("Samsung unknown " + sDeviceName + " detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_CAPTIVATE) || TextUtils.equals(sDeviceModel, DEVICE_ID_CAPTIVATE)) {
            Log.i("ATT, Samsung Captivate detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_VIBRANT) || TextUtils.equals(sDeviceModel, DEVICE_ID_VIBRANT)) {
            Log.i("T-Mobile US, Samsung Vibrant detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_EPIC) || TextUtils.equals(sDeviceModel, DEVICE_ID_EPIC)) {
            Log.i("Sprint, Samsung Epic 4G detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_FASCINATE) || TextUtils.equals(sDeviceModel, DEVICE_ID_FASCINATE)) {
            Log.i("Verizon, Samsung Fascinate detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        if (TextUtils.equals(sDeviceName, DEVICE_ID_MESMERIZE) || TextUtils.equals(sDeviceModel, DEVICE_ID_MESMERIZE)) {
            Log.i("Samsung Mesmerize detected");
            isSamsungGalaxyShit = true;
            return true;
        }

        return false;
    }


    public static boolean hasImageCaptureBug() {
        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/"
                + android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);
    }

    /**
     * If device has BT bug with following exception
     * Fatal Exception: java.lang.IllegalStateException: BT Adapter is not turned ON
     * at android.bluetooth.le.BluetoothLeUtils.checkAdapterStateOn(BluetoothLeUtils.java:136)
     * at android.bluetooth.le.BluetoothLeScanner$1.handleMessage(BluetoothLeScanner.java:85)
     * at android.os.Handler.dispatchMessage(Handler.java:102)
     * at android.os.Looper.loop(Looper.java:168)
     * at android.app.ActivityThread.main(ActivityThread.java:5885)
     * at java.lang.reflect.Method.invoke(Method.java)
     * at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:797)
     * at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:687)
     * <p/>
     * list of known devices with BT bug, HTC + Android M only!
     * HTC One (E8) dual sim
     * HTC Desire 630 dual sim
     * HTC ONE M8s
     * HTC One (E8)
     * HTC Desire EYE
     * HTC Desire 820
     * HTC One A9
     * HTC One_E8
     * HTC Desire 626s
     * HTC Desrie D530
     * HTC_D820u
     * HTC One (M8 Eye)
     * HTC Desire 10 lifestyle
     * HTC Desire626s
     *
     * @return boolean
     */
    public static boolean hasBlueToothBug() {
        return sAPILevel == 23 && (sDeviceModel.startsWith("htc ") || sDeviceModel.startsWith("htc_"));
    }

    /**
     * SDK VERSION
     * <p>
     * int	BASE	October 2008: The original, first, version of Android.
     * int	BASE_1_1	February 2009: First Android update, officially called 1.1.
     * int	CUPCAKE	May 2009: Android 1.5.
     * int	CUR_DEVELOPMENT	Magic version number for a current development build, which has not yet turned into an official release.
     * int	DONUT	September 2009: Android 1.6.
     * int	ECLAIR	November 2009: Android 2.0
     * <p>
     * Applications targeting this or a later release will get these new changes in behavior:
     * The Service.onStartCommand function will return the new START_STICKY behavior instead of the old compatibility START_STICKY_COMPATIBILITY.
     * int	ECLAIR_0_1	December 2009: Android 2.0.1
     * int	ECLAIR_MR1	January 2010: Android 2.1
     * int	FROYO	June 2010: Android 2.2
     * int	GINGERBREAD	November 2010: Android 2.3
     * <p>
     * Applications targeting this or a later release will get these new changes in behavior:
     * The application's notification icons will be shown on the new dark status bar background, so must be visible in this situation.
     * int	GINGERBREAD_MR1	February 2011: Android 2.3.3.
     * int	HONEYCOMB	February 2011: Android 3.0.
     * int	HONEYCOMB_MR1	May 2011: Android 3.1.
     * int	HONEYCOMB_MR2	June 2011: Android 3.2.
     * int	ICE_CREAM_SANDWICH	October 2011: Android 4.0.
     * int	ICE_CREAM_SANDWICH_MR1	December 2011: Android 4.0.3.
     * int	JELLY_BEAN	June 2012: Android 4.1.
     * int	JELLY_BEAN_MR1	Android 4.2: Moar jelly beans!
     * <p>
     * Applications targeting this or a later release will get these new changes in behavior:
     * Content Providers: The default value of android:exported is now false.
     * int	JELLY_BEAN_MR2	Android 4.3: Jelly Bean MR2, the revenge of the beans.
     * <p/>
     * Its a general method which detects if device supports exactly given API level
     * Actually its equality comparison like Build.VERSION.SDK_INT == apiLevel
     *
     * @param apiLevel, integer, the API level to check
     * @return
     */
    public static boolean isAPI(final int apiLevel) {
        return sAPILevel == apiLevel;
    }

    /**
     * General method which detects if device supports given API level
     * Actually its greater or equal comparison like Build.VERSION.SDK_INT >= apiLevel
     *
     * @param apiLevel, integer, the API level to check support
     * @return
     */
    public static boolean hasAPI(final int apiLevel) {
        return sAPILevel >= apiLevel;
    }

}
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

    private static boolean isInitialized;
    private static Context appContext;

    private static Boolean isHiResDisplay;

    private static boolean hasTelephony;
    //private static Boolean hasCamera;

    public static int displayDensity;
    public static int displayHeight;
    public static int displayWidth;
    public static int displayPortraitHeight;
    public static int displayPortraitWidth;

    //public static int statusBarHeight;

    public static int screenSize;
    public static int screenInches;
    public static float screenInchesByMetrics;
    public static float screenInchesByConfig;

    public static String deviceModel;
    public static String deviceManufacturer;
    public static String deviceProduct;
    public static String deviceName;
    public static String deviceARM;
    public static String[] deviceARMs;

    public static float configurationRatio;

    public static DisplayMetrics displayMetrics;

    public static final int hasAPILevel = Build.VERSION.SDK_INT;

    public static int telephonyType;

    public static boolean hasPermanentMenuKeys;
    public static boolean hasNavigationBar;
    public static int navigationBarHeight;
    public static int statusBarHeight;

    /**
     * Obtaining screen width and height.
     * Determining the type of the device e.g. hdpi,mdpi,ldpi
     */
    public static Boolean isHiResDisplay() {
        return isHiResDisplay;
    }

    /**
     * To determine device portrait width
     *
     * @return int  smallest device screen side size
     */
    public static int getSmallestScreenSideSize() {
        return Math.min(displayHeight, displayWidth);
    }

    /**
     * To determine device portrait height
     *
     * @return int  greatest device screen side size
     */
    public static int getBiggestScreenSideSize() {
        return Math.max(displayHeight, displayWidth);
    }


    /**
     * @return boolean  true if initialized successfully
     */
    @SuppressLint("NewApi")
    public static boolean init(final Context context) {

        if (isInitialized)
            return true;

        if (context == null)
            return false;

        appContext = context.getApplicationContext(); // to be sure its appContext
        displayMetrics = appContext.getResources().getDisplayMetrics();
        displayDensity = displayMetrics.densityDpi;
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        displayPortraitHeight = getBiggestScreenSideSize();
        displayPortraitWidth = getSmallestScreenSideSize();

        final Resources resources = appContext.getResources();

        final Display display = ((WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int realDisplayHeight = displayHeight, realDisplayWidth = displayWidth;
        if (hasAPILevel < 14) {
//            realDisplayHeight = displayMetrics.heightPixels;
//            realDisplayWidth =  displayMetrics.widthPixels;
        } else if (hasAPILevel > 13 && hasAPILevel < 17) {
            // includes window decorations (statusbar bar/menu bar) 14,15,16 api levels
            try {
                realDisplayWidth = (int) Display.class.getMethod("getRawWidth").invoke(display);
                realDisplayHeight = (int) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception ignored) {
            }
        } else /*if (hasAPILevel >= 17)*/ {
            // includes window decorations (statusbar bar/menu bar)
            try {
                final Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                realDisplayWidth = realSize.x;
                realDisplayHeight = realSize.y;
            } catch (Exception ignored) {
            }
        }

        hasPermanentMenuKeys = displayHeight == realDisplayHeight;

        // http://stackoverflow.com/a/28983720/1811719
        final boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        final boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        hasNavigationBar = !(hasBackKey && hasHomeKey);
        if (hasNavigationBar) {
            //The device has a navigation bar
            final int orientation = resources.getConfiguration().orientation;
            int resourceId;
            if (isTablet()) {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }
            if (resourceId > 0) {
                navigationBarHeight = resources.getDimensionPixelSize(resourceId);
            }
        }
        // Status Bar Height
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight = (int) Math.ceil(24 * displayMetrics.density);
        }
//        else {
//            final Rect rect = new Rect();
//            appContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//            statusBarHeight = rect.top;
//        }

        deviceModel = android.os.Build.MODEL.toLowerCase(Locale.US);
        deviceManufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.US);
        deviceProduct = android.os.Build.PRODUCT.toLowerCase(Locale.US);
        deviceName = android.os.Build.DEVICE.toLowerCase(Locale.US);

        final double xDensity = Math.pow(realDisplayWidth / displayMetrics.xdpi, 2);
        final double yDensity = Math.pow(realDisplayHeight / displayMetrics.ydpi, 2);
        screenInchesByMetrics = Math.round(Math.sqrt(xDensity + yDensity) * 10f) / 10f;
        screenInches = Math.round(screenInchesByMetrics);

        Log.i(String.format("Model: %s, Manufacturer: %s Product: %s Name: %s", deviceModel, deviceManufacturer, deviceProduct, deviceName));
        Log.i("Device platform: ABI: " + Build.CPU_ABI + " ABI2: " + Build.CPU_ABI2);

        deviceARM = Build.CPU_ABI;
        deviceARMs = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        if (hasAPI(21)) {
            if (Build.SUPPORTED_ABIS != null)
                deviceARMs = Build.SUPPORTED_ABIS;
            if (deviceARMs.length > 0)
                deviceARM = Build.SUPPORTED_ABIS[0];
        }

        final Configuration conf = appContext.getResources().getConfiguration();
        Log.i(String.format("Screen conf.screenHeightDp: %s, conf.screenWidthDp: %s", conf.screenHeightDp, conf.screenWidthDp));
        screenInchesByConfig = Math.round(Math.sqrt(conf.screenHeightDp * conf.screenHeightDp + conf.screenWidthDp * conf.screenWidthDp) * 10f) / 10f;

        final int screenLayout = conf.screenLayout;
//        int screenLayout = 1; // application default behavior
//        try {
//            Field field = conf.getClass().getDeclaredField("screenLayout");
//            screenLayout = field.getInt(conf);
//        } catch (Exception e) {
//        	e.printStackTrace();

        // NoSuchFieldException or related stuff
//        }

        // Configuration.SCREENLAYOUT_SIZE_MASK == 15
        screenSize = screenLayout & 15;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                //Toast.makeText(activity, "SCREENLAYOUT_SIZE_SMALL", Toast.LENGTH_SHORT).show();
                configurationRatio = .75f;
                break;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                //Toast.makeText(activity, "SCREENLAYOUT_SIZE_NORMAL", Toast.LENGTH_SHORT).show();
                configurationRatio = 1f;
                break;

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                //Toast.makeText(activity, "SCREENLAYOUT_SIZE_LARGE", Toast.LENGTH_SHORT).show();
                configurationRatio = 1.5f;
                break;

            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                configurationRatio = 2f;
                //Toast.makeText(activity, "SCREENLAYOUT_SIZE_XLARGE", Toast.LENGTH_SHORT).show();
                break;

            default:
                // undefined
                break;
        }

        Log.i(String.format("Display: Density %d, Width: %d Height: %d configurationRatio: %f", displayDensity, displayWidth, displayHeight, configurationRatio));
        Log.i(String.format("Display: DensityDpi %d, Density %f, Width: %d Height: %d", displayMetrics.densityDpi, displayMetrics.density, displayMetrics.widthPixels, displayMetrics.heightPixels));

        checkHasTelephony(appContext);

        isInitialized = true;

        return true;
    }

    public static int getStatusBarHeight(final Activity activity) {
        final Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    public static int getStatusBarHeight(final Context context) {
        if (!isInitialized)
            init(context);
        return statusBarHeight;
    }

    public static int getNavigationBarHeight(final Context context) {
        if (!isInitialized)
            init(context);
        return navigationBarHeight;
    }

    public static boolean isTablet() {
        Log.i("this device DeviceInfo.screenSize: " + DeviceInfo.screenSize + " " + DeviceInfo.displayDensity+"\n"+
              "screenInchesByMetrics: " + DeviceInfo.screenInchesByMetrics + " screenInchesByConfig: " + DeviceInfo.screenInchesByConfig);
        final boolean isTabletByResources = appContext.getResources().getBoolean(R.bool.isTablet);
        //final boolean isTabletByTelephony = !DeviceInfo.hasTelephony();
        final boolean isTabletByScreen = DeviceInfo.screenSize > 2 && DeviceInfo.screenInchesByMetrics > 7f;
        //DeviceInfo.isTabletByScreen();
        Log.i("isTabletByResources: " + isTabletByResources + " isTabletByScreen: " + isTabletByScreen + " DeviceInfo.screenSize: " + DeviceInfo.screenSize);
        return isTabletByResources || isTabletByScreen;
    }

    public static float px2dp(float px) {
        if (isInitialized)
            return (float) ((px / displayMetrics.density) + 0.5);
            //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, displayMetrics);
        else {
            Log.w("Class is not initialized!!! Method  px2dp() returns 0");
            return 0;
        }
    }

    public static float dp2px(float dp) {
        if (isInitialized)
            return (float) ((dp * displayMetrics.density) + 0.5);
            //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
        else {
            Log.w("Class is not initialized!!! Method  dp2px() returns 0");
            return 0;
        }
    }

    public static float sp2px(float sp) {
        if (isInitialized)
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics);
        else {
            Log.w("Class is not initialized!!! Method  dp2px() returns 0");
            return 0;
        }
    }


    public static boolean isScreenSizeSmall() {
        return screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    public static boolean isScreenSizeNormal() {
        return screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public static boolean isScreenSizeLarge() {
        return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isScreenSizeXLarge() {
        return screenSize == 4; //Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public static float getConfigurationRatio() {
        return configurationRatio;
    }

    public static boolean hasTelephony() {
        return hasTelephony;
    }

    public static void checkHasTelephony() {
        checkHasTelephony(appContext);
    }

    public static void checkHasTelephony(Context context) {
        // isInitialized==true means this method was already executed
//        if (!isInitialized) {
        TelephonyManager telephonyManager;
        try { // for the firecase
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Throwable e) {
            hasTelephony = false;
            return;
        }
        telephonyType = telephonyManager == null ? TelephonyManager.PHONE_TYPE_NONE : telephonyManager.getPhoneType();
        if (telephonyType == TelephonyManager.PHONE_TYPE_NONE) {
            hasTelephony = false;
            if (telephonyManager == null)
                Log.i("Has NO telephony: TelephonyManager is null");
            else
                Log.i("Has NO telephony: TelephonyManager.getPhoneType == PHONE_TYPE_NONE");
        } else {
            hasTelephony = true;
            //Phone Type
            // The getPhoneType() returns the device type. This method returns one of the following values:
            switch (telephonyType) {
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
                    Log.i("Has UNKNOWN TYPE Telephony: " + telephonyType);
                    break;
            }
        }
//        }
    }

    @SuppressLint("NewApi")
    public static Boolean hasCamera() {
        final PackageManager pm = appContext.getPackageManager();
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
    public static boolean hasCameraApi2(){
        try {
            final android.hardware.camera2.CameraManager cameraManager = (android.hardware.camera2.CameraManager) appContext.getSystemService(Context.CAMERA_SERVICE);
            final String[] cameraIdList = cameraManager.getCameraIdList();
            return cameraIdList!=null && cameraIdList.length > 0;
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
        final PackageManager pm = appContext.getPackageManager();
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
        return getDeviceUuid(appContext);
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
            prefs.edit().putString(PREFS_DEVICE_UUID_KEY, uuid).commit();
        }

        return uuid;
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or Secure.ANDROID_ID
     *
     * @return a ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceId() {
        return getDeviceId(appContext);
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
        return getDeviceIMEI(appContext);
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or null
     *
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceIMEI(Context context) {

        if (!isInitialized)
            checkHasTelephony(context);

        String deviceId = null;

        if (hasTelephony) {
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
        if (isHiResDisplay != null) {
            switch (displayDensity) {
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
        }
        return maxSideSize;
    }

    /**
     * Tries to determine device is Tablet or SmartPhone by its screen
     *
     * @return
     */
    public static Boolean isTabletByScreen() {

        if (!isInitialized)
            return null;

        boolean byScreen = screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
                & screenInches >= 7
                & (displayDensity == DisplayMetrics.DENSITY_DEFAULT
                || displayDensity == DisplayMetrics.DENSITY_HIGH
                || displayDensity == DisplayMetrics.DENSITY_MEDIUM
                || displayDensity == 213 //DisplayMetrics.DENSITY_TV
                || displayDensity == 320 /*DisplayMetrics.DENSITY_XHIGH*/
        );
        // If >Large, checks if the Generalized Density is at least MDPI
        // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160,
        // DENSITY_TV=213, DENSITY_XHIGH=320

        return byScreen;
    }

    /**
     * Tries to figure out if the current phone is Galaxy S based, since it has
     * recording issues This is pretty nasty since we are string matching, but
     * unless I can get a better way to do it...
     *
     * @param
     */
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

    public static boolean isSamsungGalaxyShit() {
        if (isSamsungGalaxyShit != null)
            return isSamsungGalaxyShit;

        deviceManufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.US);
        //String model = android.os.Build.MODEL.toLowerCase();
        deviceName = android.os.Build.DEVICE.toLowerCase(Locale.US);
        //Log.i(TAG, String.format("deviceManufacturer: %s, model: %s, device: %s", deviceManufacturer, model, device));

        // нуллы случаются в deviceManufacturer и/или deviceName/deviceModel когда на выходе
        // умирающий сервис пытается что-то выведать, а именно запускает этот метод
        if (deviceManufacturer == null || deviceName == null || deviceModel == null)
            return false;


        if (deviceManufacturer.equals(MANUFACTURER_SAMSUNG)) {
            if (deviceName.equals(DEVICE_ID_GALAXY_S) || deviceModel.equals(DEVICE_ID_GALAXY_S)) {
                Log.i("Samsung Galaxy S detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_PLUS) || deviceModel.equals(DEVICE_ID_GALAXY_S_PLUS)) {
                Log.i("Samsung Galaxy S+ detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_CRAFT) || deviceModel.equals(DEVICE_ID_GALAXY_S_CRAFT)) {
                Log.i("Samsung Galaxy S Craftman detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_SL) || deviceModel.equals(DEVICE_ID_GALAXY_SL)) {
                Log.i("Samsung Galaxy SL detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_II) || deviceModel.equals(DEVICE_ID_GALAXY_S_II)) {
                Log.i("Samsung Galaxy S II detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_II_PLUS) || deviceModel.equals(DEVICE_ID_GALAXY_S_II_PLUS)) {
                Log.i("Samsung Galaxy S II+ detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_ADVANCE) || deviceModel.equals(DEVICE_ID_GALAXY_S_ADVANCE)) {
                Log.i("Samsung Galaxy S Advance detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_III) || deviceModel.equals(DEVICE_ID_GALAXY_S_III)) {
                Log.i("Samsung Galaxy S III detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_GRAND) || deviceModel.equals(DEVICE_ID_GALAXY_S_GRAND)) {
                Log.i("Samsung Galaxy Grand detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_ARMANI) || deviceModel.equals(DEVICE_ID_GALAXY_S_ARMANI)) {
                Log.i("Samsung Galaxy S Giorgio Armani detected");
                isSamsungGalaxyShit = true;
                return true;
            }


            if (deviceName.contains("DEVICE_ID_GALAXY_PFX") || deviceModel.contains(DEVICE_ID_GALAXY_PFX)) {
                Log.i("Samsung unknown " + deviceName + " detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_CAPTIVATE) || deviceModel.equals(DEVICE_ID_CAPTIVATE)) {
                Log.i("ATT, Samsung Captivate detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_VIBRANT) || deviceModel.equals(DEVICE_ID_VIBRANT)) {
                Log.i("T-Mobile US, Samsung Vibrant detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_EPIC) || deviceModel.equals(DEVICE_ID_EPIC)) {
                Log.i("Sprint, Samsung Epic 4G detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_FASCINATE) || deviceModel.equals(DEVICE_ID_FASCINATE)) {
                Log.i("Verizon, Samsung Fascinate detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_MESMERIZE) || deviceModel.equals(DEVICE_ID_MESMERIZE)) {
                Log.i("Samsung Mesmerize detected");
                isSamsungGalaxyShit = true;
                return true;
            }

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
     SDK VERSION

     int	BASE	October 2008: The original, first, version of Android.
     int	BASE_1_1	February 2009: First Android update, officially called 1.1.
     int	CUPCAKE	May 2009: Android 1.5.
     int	CUR_DEVELOPMENT	Magic version number for a current development build, which has not yet turned into an official release.
     int	DONUT	September 2009: Android 1.6.
     int	ECLAIR	November 2009: Android 2.0

     Applications targeting this or a later release will get these new changes in behavior:
     The Service.onStartCommand function will return the new START_STICKY behavior instead of the old compatibility START_STICKY_COMPATIBILITY.
     int	ECLAIR_0_1	December 2009: Android 2.0.1
     int	ECLAIR_MR1	January 2010: Android 2.1
     int	FROYO	June 2010: Android 2.2
     int	GINGERBREAD	November 2010: Android 2.3

     Applications targeting this or a later release will get these new changes in behavior:
     The application's notification icons will be shown on the new dark status bar background, so must be visible in this situation.
     int	GINGERBREAD_MR1	February 2011: Android 2.3.3.
     int	HONEYCOMB	February 2011: Android 3.0.
     int	HONEYCOMB_MR1	May 2011: Android 3.1.
     int	HONEYCOMB_MR2	June 2011: Android 3.2.
     int	ICE_CREAM_SANDWICH	October 2011: Android 4.0.
     int	ICE_CREAM_SANDWICH_MR1	December 2011: Android 4.0.3.
     int	JELLY_BEAN	June 2012: Android 4.1.
     int	JELLY_BEAN_MR1	Android 4.2: Moar jelly beans!

     Applications targeting this or a later release will get these new changes in behavior:
     Content Providers: The default value of android:exported is now false.
     int	JELLY_BEAN_MR2	Android 4.3: Jelly Bean MR2, the revenge of the beans.
     */

    /**
     * General method which detects if device supports given API level
     *
     * @param mApiLevel
     * @return
     */
    public static boolean hasAPI(final int mApiLevel) {
        return Build.VERSION.SDK_INT >= mApiLevel;
    }

    /**
     * If device SUPPORTS API level 7, these are all devices starting from API level 7
     *
     * @return
     */
    public static boolean hasAPI7() { //ECLAIR_MR1
        return hasAPI(7);
    }

    public static boolean hasAPI8() { //FROYO;
        return hasAPI(8);
    }

    public static boolean hasAPI9() { //GINGERBREAD
        return hasAPI(9);
    }

    public static boolean hasAPI10() { //GINGERBREAD_MR1
        return hasAPI(10);
    }

    public static boolean hasAPI11() { //HONEYCOMB
        return hasAPI(11);
    }

    public static boolean hasAPI12() { //HONEYCOMB_MR1
        return hasAPI(12);
    }

    public static boolean hasAPI13() { //HONEYCOMB_MR2
        return hasAPI(13);
    }

    public static boolean hasAPI14() { //ICE_CREAM_SANDWICH
        return hasAPI(14);
    }

    public static boolean hasAPI15() { //ICE_CREAM_SANDWICH_MR1
        return hasAPI(15);
    }

    public static boolean hasAPI16() { //JELLY_BEAN
        return hasAPI(16);
    }

    public static boolean hasAPI17() { //JELLY_BEAN_MR1
        return hasAPI(17);
    }

    public static boolean hasAPI18() { //JELLY_BEAN_MR2
        return hasAPI(18);
    }

    public static boolean hasAPI19() { //KITKAT
        return hasAPI(19);
    }

    public static boolean hasAPI20() {
        return hasAPI(20);
    }

    public static boolean hasAPI21() { //LOL
        return hasAPI(21);
    }

    public static boolean hasAPI22() {
        return hasAPI(22);
    }

    public static boolean isAPI7() { //ECLAIR_MR1
        return Build.VERSION.SDK_INT == 7;
//        Boolean isAPI7 = null;
//        try{
//            isAPI7 = hasAPILievel >= 7; //Build.VERSION_CODES.ECLAIR_MR1;
//        } catch (Exception e){}
//
//        if (isAPI7 == null)
//            isAPI7 = Build.VERSION.RELEASE.startsWith("2.1");
//
//        return isAPI7.booleanValue();
    }

    public static boolean isAPI8() { //FROYO;
        return Build.VERSION.SDK_INT == 8;
//        Boolean isAPI8 = null;
//        try{
//            isAPI8 = hasAPILievel >= 8; //Build.VERSION_CODES.FROYO;
//        } catch (Exception e){}
//
//        if (isAPI8 == null)
//            isAPI8 = Build.VERSION.RELEASE.startsWith("2.2");
//
//        return isAPI8.booleanValue();
    }

    public static boolean isAPI9() { //GINGERBREAD
        return Build.VERSION.SDK_INT == 9;
//        Boolean isAPI9 = null;
//        try{
//            isAPI9 = hasAPILievel >= 9; //Build.VERSION_CODES.GINGERBREAD;
//        } catch (Exception e){}
//
//        if (isAPI9 == null)
//            isAPI9 = Build.VERSION.RELEASE.startsWith("2.3") &&  !Build.VERSION.RELEASE.startsWith("2.3.");
//
//        return isAPI9.booleanValue();
    }

    public static boolean isAPI10() { //GINGERBREAD_MR1
        return Build.VERSION.SDK_INT == 10;
//        Boolean isAPI10 = null;
//        try{
//            isAPI10 = hasAPILievel >= 10; //Build.VERSION_CODES.GINGERBREAD_MR1;
//        } catch (Exception e){}
//
//        if (isAPI10 == null)
//            isAPI10 = Build.VERSION.RELEASE.startsWith("2.3.3") || Build.VERSION.RELEASE.startsWith("2.3.4") || Build.VERSION.RELEASE.startsWith("2.3.5");
//
//        return isAPI10.booleanValue();
    }

    public static boolean isAPI11() { //HONEYCOMB
        return Build.VERSION.SDK_INT == 11;
//        Boolean isAPI11 = null;
//        try{
//            isAPI11 = hasAPILievel >= 11; //Build.VERSION_CODES.HONEYCOMB;
//        } catch (Exception e){}
//
//        if (isAPI11 == null)
//            isAPI11 = Build.VERSION.RELEASE.startsWith("3.0");
//
//        return isAPI11.booleanValue();
    }

    public static boolean isAPI12() { //HONEYCOMB_MR1
        return Build.VERSION.SDK_INT == 12;
//        Boolean isAPI12 = null;
//        try{
//            isAPI12 = hasAPILievel >= 12; //Build.VERSION_CODES.HONEYCOMB_MR1;
//        } catch (Exception e){}
//
//        if (isAPI12 == null)
//            isAPI12 = Build.VERSION.RELEASE.startsWith("3.1");
//
//        return isAPI12.booleanValue();
    }

    public static boolean isAPI13() { //HONEYCOMB_MR2
        return Build.VERSION.SDK_INT == 13;
//        Boolean isAPI13 = null;
//        try{
//            isAPI13 = hasAPILievel >= 13; //Build.VERSION_CODES.HONEYCOMB_MR2;
//        } catch (Exception e){}
//
//        if (isAPI13 == null)
//            isAPI13 = Build.VERSION.RELEASE.startsWith("3.2");
//
//        return isAPI13.booleanValue();
    }

    public static boolean isAPI14() { //ICE_CREAM_SANDWICH
        return Build.VERSION.SDK_INT == 14;
//        Boolean isAPI14 = null;
//        try{
//            isAPI14 = hasAPILievel >= 14; //Build.VERSION_CODES.ICE_CREAM_SANDWICH;
//        } catch (Exception e){}
//
//        if (isAPI14 == null)
//            isAPI14 = Build.VERSION.RELEASE.startsWith("4.0");
//
//        return isAPI14.booleanValue();
    }

    public static boolean isAPI15() { //ICE_CREAM_SANDWICH_MR1
        return Build.VERSION.SDK_INT == 15;
//        Boolean isAPI15 = null;
//        try{
//            isAPI15 = hasAPILievel >= 15; //Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
//        } catch (Exception e){}
//
//        if (isAPI15 == null)
//            isAPI15 = Build.VERSION.RELEASE.startsWith("4.0.3");
//
//        return isAPI15.booleanValue();
    }

    public static boolean isAPI16() { //JELLY_BEAN
        return Build.VERSION.SDK_INT == 16;
//        Boolean isAPI16 = null;
//        try{
//            isAPI16 = hasAPILievel >= 16; //Build.VERSION_CODES.JELLY_BEAN;
//        } catch (Exception e){}
//
//        if (isAPI16 == null)
//            isAPI16 = Build.VERSION.RELEASE.startsWith("4.1");
//
//        return isAPI16.booleanValue();
    }

    public static boolean isAPI17() { //JELLY_BEAN_MR1
        return Build.VERSION.SDK_INT == 17;
//        Boolean isAPI17 = null;
//        try{
//            isAPI17 = hasAPILievel >= 17; //Build.VERSION_CODES.JELLY_BEAN_MR1;
//        } catch (Exception e){}
//
//        if (isAPI17 == null)
//            isAPI17 = Build.VERSION.RELEASE.startsWith("4.2");
//
//        return isAPI17.booleanValue();
    }

    public static boolean isAPI18() { //JELLY_BEAN_MR2
        return Build.VERSION.SDK_INT == 18;
//        Boolean isAPI18 = null;
//        try{
//            isAPI18 = hasAPILievel >= 18; //JELLY_BEAN_MR2;
//        } catch (Exception e){}
//
//        if (isAPI18 == null)
//            isAPI18 = Build.VERSION.RELEASE.startsWith("4.3");
//
//        return isAPI18.booleanValue();
    }

    public static boolean isAPI19() { //KITKAT
        return Build.VERSION.SDK_INT == 18;
//        Boolean isAPI19 = null;
//        try{
//            isAPI19 = hasAPILievel >= 19; //Build.VERSION_CODES.KITKAT;
//        } catch (Exception e){}
//
//        if (isAPI19 == null)
//            isAPI19 = Build.VERSION.RELEASE.startsWith("4.4");
//
//        return isAPI19.booleanValue();
    }

    public static boolean isAPI20() {
        return Build.VERSION.SDK_INT == 20;
//        Boolean isAPI20 = hasAPILievel >= 20;
//        return isAPI20.booleanValue();
    }

    public static boolean isAPI21() { //LOLIPOP
        return Build.VERSION.SDK_INT == 21;
//        Boolean isAPI21 = hasAPILievel >= 21;
//        return isAPI21.booleanValue();
    }

    public static boolean isAPI22() {
        return Build.VERSION.SDK_INT == 22;
//        Boolean isAPI22 = hasAPILievel >= 22;
//        return isAPI22.booleanValue();
    }
}
package com.stanko.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

/*
 * This class requires INITIALIZATION!!! to work properly
 */
public class DeviceInfo {

    private static final String LOGTAG = Log.getLogTag(DeviceInfo.class);

    private static boolean isInitialzed;
    private static Context appContext;

    private static Boolean isHiResDisplay;

    private static boolean hasTelephony;
    //private static Boolean hasCamera;

    public static int displayDensity;
    public static int displayHeight;
    public static int displayWidth;

    //public static int statusBarHeight;

    public static int screenSize;
    public static int screenInches;

    public static String deviceModel;
    public static String deviceManufacturer;
    public static String deviceProduct;
    public static String deviceName;
    public static String deviceARM;
    public static String[] deviceARMs;

    private static float configurationRatio;

    private static DisplayMetrics displayMetrics;

    public static final int hasAPILievel = Build.VERSION.SDK_INT;

    /**
     * Obtaining screen width and height.
     * Determining the type of the device e.g. hdpi,mdpi,ldpi
     * This will be saved in a sharedPreferences
     */
    public static Boolean isHiResDisplay() {
        return isHiResDisplay;
    }

    /** Returns the smallest device screen side size
     *
     * @return int  smallest device screen side size
     */
    public static int getSmallestScreenSideSize() {
        return Math.min(displayHeight, displayWidth);
    }

    //@SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static synchronized boolean init(final Context context) {

        if (isInitialzed)
            return true;

        if (context==null)
            return false;

        appContext = context.getApplicationContext();
//		Display display = activity.getWindowManager().getDefaultDisplay();
        //display.getMetrics(displayMetrics);
        displayMetrics =  appContext.getResources().getDisplayMetrics();
        displayDensity = displayMetrics.densityDpi;

        //Display display = getWindowManager().getDefaultDisplay();
//		if (Build.VERSION.SDK_INT < 13){
//			displayHeight = display.getHeight(); //displayMetrics.heightPixels; //
//			displayWidth =  display.getWidth(); //displayMetrics.widthPixels;
//		}
//		else{
        displayHeight = displayMetrics.heightPixels;
        displayWidth =  displayMetrics.widthPixels;
//			Point size = new Point();
//			display.getSize(size);
//			displayHeight = size.x;
//			displayWidth = size.y;
//		}

        deviceModel = android.os.Build.MODEL.toLowerCase(Locale.US);
        deviceManufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.US);
        deviceProduct = android.os.Build.PRODUCT.toLowerCase(Locale.US);
        deviceName = android.os.Build.DEVICE.toLowerCase(Locale.US);

        double xDensity = Math.pow(displayWidth/displayMetrics.xdpi,2);
        double yDensity = Math.pow(displayHeight/displayMetrics.ydpi,2);
        screenInches = (int)Math.round(Math.sqrt(xDensity+yDensity));

        Log.i(LOGTAG,String.format("Model: %s, Manufacturer: %s Product: %s Name: %s",deviceModel,deviceManufacturer, deviceProduct,deviceName));

        Log.i(LOGTAG, "Device platform: ABI: " + Build.CPU_ABI + " ABI2: " + Build.CPU_ABI2);

        deviceARM = Build.CPU_ABI;
        deviceARMs = new String[]{Build.CPU_ABI,Build.CPU_ABI2};
        if (hasAPI(21)) {
            if (Build.SUPPORTED_ABIS!=null);
                deviceARMs = Build.SUPPORTED_ABIS;
            if (deviceARMs.length>0)
                deviceARM = Build.SUPPORTED_ABIS[0];
        }

        Configuration conf = appContext.getResources().getConfiguration();
        int screenLayout = conf.screenLayout;
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

        Log.i(LOGTAG,String.format("Display: Density %d, Width: %d Height: %d configurationRatio: %f",displayDensity,displayWidth, displayHeight,configurationRatio));
        Log.i(LOGTAG,String.format("Display: DensityDpi %d, Density %f, Width: %d Height: %d",displayMetrics.densityDpi,displayMetrics.density,displayMetrics.widthPixels, displayMetrics.heightPixels));

        checkHasTelephony(appContext);

        isInitialzed = true;

        return true;
    }

    public static int getstatusBarHeight(final Activity activity){
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    public static float px2dp(float px){
        if(isInitialzed)
            return (float)((px /displayMetrics.density) + 0.5);
            //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, displayMetrics);
        else{
            Log.w(LOGTAG,"Class is not initialized!!! Method  px2dp() returns 0");
            return 0;
        }
    }
    public static float dp2px(float dp){
        if(isInitialzed)
            return (float) ((dp * displayMetrics.density) + 0.5);
            //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
        else{
            Log.w(LOGTAG,"Class is not initialized!!! Method  dp2px() returns 0");
            return 0;
        }
    }
    public static float sp2px(float sp){
        if(isInitialzed)
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics);
        else{
            Log.w(LOGTAG,"Class is not initialized!!! Method  dp2px() returns 0");
            return 0;
        }
    }


    public static boolean isScreenSizeSmall(){
        return screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }
    public static boolean isScreenSizeNormal(){
        return screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }
    public static boolean isScreenSizeLarge(){
        return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    public static boolean isScreenSizeXLarge(){
        return screenSize == 4; //Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
    public static float getConfigurationRatio(){
        return configurationRatio;
    }

    public static boolean hasTelephony(){
        return hasTelephony;
    }

    public static void checkHasTelephony(){
        checkHasTelephony(appContext);
    }
    public static void checkHasTelephony(Context context)
    {
        if(!isInitialzed)
        {
            TelephonyManager telephonyManager = null;
            try{ // for the firecase
                telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            } catch(Exception e){
                hasTelephony = false;
                return;
            } catch(Throwable e){
                hasTelephony = false;
                return;
            }

            if (telephonyManager == null || telephonyManager.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE){
                hasTelephony = false;
                if (telephonyManager == null)
                    Log.i(LOGTAG, "Has NO telephony via telephonyManager == null!");
                else
                    Log.i(LOGTAG, "Has NO telephony via getPhoneType==PHONE_TYPE_NONE");
                return;
            } else{
                //Phone Type
                // The getPhoneType() returns the device type. This method returns one of the following values:
                if (telephonyManager.getPhoneType()!=TelephonyManager.PHONE_TYPE_NONE){
                    hasTelephony = true;
                    Log.i(LOGTAG, "HAS!!! Telephony!");
                    return;
                }
//				if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM || 
//						telephonyManager.getPhoneType()TelephonyManager.PHONE_TYPE_CDMA
            }

            PackageManager pm = context.getPackageManager();

            if(pm!=null)
            {
                hasTelephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
                Log.i(LOGTAG, "PackageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY):"+hasTelephony);
                //hasCamera = Boolean.valueOf(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA));

//	            try
//	            {
//	                Class[] parameters=new Class[1];
//	                parameters[0]=String.class;
//	                method=pm.getClass().getMethod("hasSystemFeature", parameters);
//	                Object[] parm=new Object[1];
//	                parm[0]=new String(PackageManager.FEATURE_TELEPHONY);
//	                Object retValue=method.invoke(pm, parm);
//	                if(retValue instanceof Boolean)
//	                    hasTelephony=Boolean.valueOf(((Boolean) retValue).booleanValue());
//	                else
//	                    hasTelephony=Boolean.valueOf(false);
//	            }
//	            catch(Exception e)
//	            {
//	                hasTelephony=Boolean.valueOf(false);
//	            }
            }
        }
        return;
    }


    // unique key for sharedprefs to store generated UUID
    private static final String PREFS_DEVICE_INFO_KEY = "DeviceInfo";
    private static final String PREFS_DEVICE_UUID_KEY = Hash.getMD5("DeviceInfoGeneratedUUID");
    private static String uuid;

    /**
     * Returns a unique String UUID for the current android device.  
     * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
     * @return an UUID that may be used to uniquely identify your device for most purposes.
     */
    public static synchronized String getDeviceUuid() {
        return getDeviceUuid(appContext);
    }
    /**
     * Returns a unique String UUID for the current android device.
     * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an UUID that may be used to uniquely identify your device for most purposes.
     */
    public static synchronized String getDeviceUuid(Context context) {
        // 1st start it'd b null
        if(uuid != null)
            return uuid;

        // lets try to restore DeviceUuid from SharedPrefs
        final SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_DEVICE_INFO_KEY, Context.MODE_PRIVATE);
        final String id = prefs.getString(PREFS_DEVICE_UUID_KEY, null );

        if ( !TextUtils.isEmpty(id) )
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
     * @return a ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceId(){
        return getDeviceId(appContext);
    }
    /**
     * Returns a unique String ID for the current android device based on IMEI or Secure.ANDROID_ID
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceId(Context context){

        String deviceId = getDeviceIMEI(context);

        if (TextUtils.isEmpty(deviceId) || deviceId.length()<8)
            deviceId = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);

        return deviceId;
    }

    /**
     * Returns a unique String ID for the current android device based on IMEI or null
     * @return a ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceIMEI(){
        return getDeviceIMEI(appContext);
    }
    /**
     * Returns a unique String ID for the current android device based on IMEI or null
     * @param context - Context (to be able to work with SharedPrefs)
     * @return an ID that may be used to uniquely identify your device for most purposes.
     */
    public static String getDeviceIMEI(Context context){

        if (!isInitialzed)
            checkHasTelephony(context);

        String deviceId = null;

        if (hasTelephony) {
            final TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager!=null)
                deviceId = telephonyManager.getDeviceId();
        }

        return deviceId;
    }

    /**
     * Returns common and safe image maximum side side according to device display density
     *
     * @return
     */
    public static int getDeviceMaxSideSizeByDensity(){
        int maxSideSize=128;
        if (isHiResDisplay!=null)
        {
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
    public static Boolean isTabletByScreen(){

        if (!isInitialzed)
            return null;

        boolean byScreen = screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
                & screenInches >=7
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
        if (isSamsungGalaxyShit!=null)
            return isSamsungGalaxyShit;

        deviceManufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.US);
        //String model = android.os.Build.MODEL.toLowerCase();
        deviceName = android.os.Build.DEVICE.toLowerCase(Locale.US);
        //Log.i(TAG, String.format("deviceManufacturer: %s, model: %s, device: %s", deviceManufacturer, model, device));

        // нуллы случаются в deviceManufacturer и/или deviceName/deviceModel когда на выходе
        // умирающий сервис пытается что-то выведать, а именно запускает этот метод
        if (deviceManufacturer==null || deviceName==null || deviceModel==null)
            return false;


        if (deviceManufacturer.equals(MANUFACTURER_SAMSUNG)) {
            if (deviceName.equals(DEVICE_ID_GALAXY_S) || deviceModel.equals(DEVICE_ID_GALAXY_S)) {
                Log.i(LOGTAG, "Samsung Galaxy S detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_PLUS) || deviceModel.equals(DEVICE_ID_GALAXY_S_PLUS)) {
                Log.i(LOGTAG, "Samsung Galaxy S+ detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_CRAFT) || deviceModel.equals(DEVICE_ID_GALAXY_S_CRAFT)) {
                Log.i(LOGTAG, "Samsung Galaxy S Craftman detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_SL) || deviceModel.equals(DEVICE_ID_GALAXY_SL)) {
                Log.i(LOGTAG, "Samsung Galaxy SL detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_II) || deviceModel.equals(DEVICE_ID_GALAXY_S_II)) {
                Log.i(LOGTAG, "Samsung Galaxy S II detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_II_PLUS) || deviceModel.equals(DEVICE_ID_GALAXY_S_II_PLUS)) {
                Log.i(LOGTAG, "Samsung Galaxy S II+ detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_ADVANCE) || deviceModel.equals(DEVICE_ID_GALAXY_S_ADVANCE)) {
                Log.i(LOGTAG, "Samsung Galaxy S Advance detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_III) || deviceModel.equals(DEVICE_ID_GALAXY_S_III)) {
                Log.i(LOGTAG, "Samsung Galaxy S III detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_GRAND) || deviceModel.equals(DEVICE_ID_GALAXY_S_GRAND)) {
                Log.i(LOGTAG, "Samsung Galaxy Grand detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_GALAXY_S_ARMANI) || deviceModel.equals(DEVICE_ID_GALAXY_S_ARMANI)) {
                Log.i(LOGTAG, "Samsung Galaxy S Giorgio Armani detected");
                isSamsungGalaxyShit = true;
                return true;
            }


            if (deviceName.contains("DEVICE_ID_GALAXY_PFX") || deviceModel.contains(DEVICE_ID_GALAXY_PFX)) {
                Log.i(LOGTAG, "Samsung unknown "+deviceName+" detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_CAPTIVATE) || deviceModel.equals(DEVICE_ID_CAPTIVATE)) {
                Log.i(LOGTAG, "ATT, Samsung Captivate detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_VIBRANT) || deviceModel.equals(DEVICE_ID_VIBRANT)) {
                Log.i(LOGTAG, "T-Mobile US, Samsung Vibrant detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_EPIC) || deviceModel.equals(DEVICE_ID_EPIC)) {
                Log.i(LOGTAG, "Sprint, Samsung Epic 4G detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_FASCINATE) || deviceModel.equals(DEVICE_ID_FASCINATE)) {
                Log.i(LOGTAG, "Verizon, Samsung Fascinate detected");
                isSamsungGalaxyShit = true;
                return true;
            }

            if (deviceName.equals(DEVICE_ID_MESMERIZE) || deviceModel.equals(DEVICE_ID_MESMERIZE)) {
                Log.i(LOGTAG, "Samsung Mesmerize detected");
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
    public static boolean hasAPI(final int mApiLevel){
        return Build.VERSION.SDK_INT >= mApiLevel;
    }

    /**
     * If device SUPPORTS API level 7, these are all devices starting from API level 7
     * @return
     */
    public static boolean hasAPI7(){ //ECLAIR_MR1
        return hasAPI(7);
    }

    public static boolean hasAPI8(){ //FROYO;
        return hasAPI(8);
    }

    public static boolean hasAPI9(){ //GINGERBREAD
        return hasAPI(9);
    }

    public static boolean hasAPI10(){ //GINGERBREAD_MR1
        return hasAPI(10);
    }

    public static boolean hasAPI11(){ //HONEYCOMB
        return hasAPI(11);
    }

    public static boolean hasAPI12(){ //HONEYCOMB_MR1
        return hasAPI(12);
    }

    public static boolean hasAPI13(){ //HONEYCOMB_MR2
        return hasAPI(13);
    }

    public static boolean hasAPI14(){ //ICE_CREAM_SANDWICH
        return hasAPI(14);
    }

    public static boolean hasAPI15(){ //ICE_CREAM_SANDWICH_MR1
        return hasAPI(15);
    }

    public static boolean hasAPI16(){ //JELLY_BEAN
        return hasAPI(16);
    }

    public static boolean hasAPI17(){ //JELLY_BEAN_MR1
        return hasAPI(17);
    }

    public static boolean hasAPI18(){ //JELLY_BEAN_MR2
        return hasAPI(18);
    }

    public static boolean hasAPI19(){ //KITKAT
        return hasAPI(19);
    }

    public static boolean hasAPI20(){
        return hasAPI(20);
    }

    public static boolean hasAPI21(){ //LOL
        return hasAPI(21);
    }

    public static boolean hasAPI22(){
        return hasAPI(22);
    }

    public static boolean isAPI7(){ //ECLAIR_MR1
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

    public static boolean isAPI8(){ //FROYO;
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

    public static boolean isAPI9(){ //GINGERBREAD
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

    public static boolean isAPI10(){ //GINGERBREAD_MR1
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

    public static boolean isAPI11(){ //HONEYCOMB
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

    public static boolean isAPI12(){ //HONEYCOMB_MR1
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

    public static boolean isAPI13(){ //HONEYCOMB_MR2
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

    public static boolean isAPI14(){ //ICE_CREAM_SANDWICH
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

    public static boolean isAPI15(){ //ICE_CREAM_SANDWICH_MR1
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

    public static boolean isAPI16(){ //JELLY_BEAN
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

    public static boolean isAPI17(){ //JELLY_BEAN_MR1
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

    public static boolean isAPI18(){ //JELLY_BEAN_MR2
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

    public static boolean isAPI19(){ //KITKAT
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

    public static boolean isAPI20(){
        return Build.VERSION.SDK_INT == 20;
//        Boolean isAPI20 = hasAPILievel >= 20;
//        return isAPI20.booleanValue();
    }

    public static boolean isAPI21(){ //LOLIPOP
        return Build.VERSION.SDK_INT == 21;
//        Boolean isAPI21 = hasAPILievel >= 21;
//        return isAPI21.booleanValue();
    }

    public static boolean isAPI22(){
        return Build.VERSION.SDK_INT == 22;
//        Boolean isAPI22 = hasAPILievel >= 22;
//        return isAPI22.booleanValue();
    }
}
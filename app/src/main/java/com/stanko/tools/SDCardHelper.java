package com.stanko.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * class used to store/restore of images using SD card.
 *
 * @author Stan Koshutsky
 *         requires one initialization with Application's packagename (not the activity's one)
 *         and context. No reference to context is stored
 */
public class SDCardHelper {

    private static final String SD_CARD_HELPER_INIT_ERR = "SDCardHelper is not initialized properly";

    private static final String SD_DEFAULT_DIR_ANDROID = "/Android";
    private static final String SD_DEFAULT_DIR_DATA = SD_DEFAULT_DIR_ANDROID + File.separator + "data" + File.separator;
    private static final String SD_DEFAULT_DIR_OBB = SD_DEFAULT_DIR_ANDROID + File.separator + "obb" + File.separator;
    private final static String SD_EXPANSION_FILES_PATH = SD_DEFAULT_DIR_OBB;

    private static final String SD_DEFAULT_PACKAGE = SDCardHelper.class.getName();

    private static String SD_CACHE_PATH = SD_DEFAULT_DIR_DATA + SD_DEFAULT_PACKAGE + File.separator + "cache" + File.separator;
    private static boolean sIsInitialized;
    private static Context sAppContext;
    private static File sInternalCacheDir;
    private static File sExternalCacheDir;

    public static void init(Context context) {
        init(context.getPackageName(), context);
    }

    public static void init(final String appPackageName, Context context) {
        SD_CACHE_PATH = String.format("/Android/data/%s/cache/", appPackageName);
        sInternalCacheDir = context.getCacheDir();
        sExternalCacheDir = new File(Environment.getExternalStorageDirectory() + SD_CACHE_PATH);
        sAppContext = context.getApplicationContext();
//        if (isExternalStorageWritable()) {
//        }
        sIsInitialized = true;
    }

    /**
     * Returns the free space in bytes available on External Storage Drive (SD card)
     * or -1 if storage is not available (UNMOUNTED, etc)
     *
     * @return long
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getAvailableSpace() {
        final String mSDRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        long availableBytez = -1;
        if (!isExternalStorageAvailable())
            return availableBytez;
        try {
            final StatFs stat = new StatFs(mSDRootPath);
            stat.restat(mSDRootPath);
            if (DeviceInfo.hasAPI(18))
                availableBytez = (stat.getAvailableBlocksLong() * stat.getBlockSizeLong());
            else
                availableBytez = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());

//				File path = Environment.getExternalStorageDirectory();
//	            StatFs stat = new StatFs(path.getPath());
//	            long blockSize = stat.getBlockSize();
//	            long totalBlocks = stat.getBlockCount();
//	            long availableBlocks = stat.getAvailableBlocks();
//
//	            mSdSize.setSummary(formatSize(totalBlocks * blockSize));
//	            mSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly);
//
//	            mSdMountToggle.setEnabled(true);
//	            mSdMountToggle.setTitle(mRes.getString(R.string.sd_eject));
//	            mSdMountToggle.setSummary(mRes.getString(R.string.sd_eject_summary));

        } catch (IllegalArgumentException e) {
            // this can occur if the SD card is removed, but we haven't received the
            // ACTION_MEDIA_REMOVED Intent yet.
            // status = Environment.MEDIA_REMOVED;
        }

        return availableBytez;
    }

    /**
     * returns External Storage Drive (SD card) root path
     *
     * @return String
     */
    public static File getSDRootPath() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * Returns a file for storing a preview of an image by its url
     * based on MD5(url) on External Storage Drive in cache directory.
     * Creates path (mkdirs) if necessary.
     *
     * @param url of image
     * @return File to save the image to or null if given url is null
     */
    public static File getFileForPreviewImageCaching(final URL url) {
        return getFileForPreviewImageCaching(url.toString());
    }

    /**
     * Returns a file for storing a preview of an image by its url
     * based on MD5(url) on External Storage Drive in cache directory.
     * Creates path (mkdirs) if necessary.
     *
     * @param sURL - String url of image
     * @return File to save the image to or null if given url is null
     */
    public static File getFileForPreviewImageCaching(final String sURL) {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        if (sURL == null)
            return null;
        final File file = getPreviewFile(sURL);
        return file;
    }

    /**
     * Returns a file to store downloading image to. The file name is
     * based on MD5(url) and its path leads to External Storage Drive
     * cache directory.	Creates path (mkdirs) if necessary.
     *
     * @param url - of an image
     * @return
     */
    public static File getFileForImageCaching(final URL url) {
        return getFileForImageCaching(url.toString());
    }

    /**
     * Returns a file to store downloading image to. The file name is
     * based on MD5(url) and its path leads to External Storage Drive
     * cache directory.	Creates path (mkdirs) if necessary.
     *
     * @param sURL - String url of an image
     * @return
     */
    public static File getFileForImageCaching(final String sURL) {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        if (sURL == null)
            return null;
        final File file = getFile(sURL);
        return file;
    }

    /**
     * Checks if given image was already cached/stored. File name is
     * based on MD5(url) and its path leads to External Storage Drive
     * cache directory.	Creates path (mkdirs) if necessary.
     *
     * @param url of an image
     * @return true if file with such name exists of false otherwise
     */
    public static boolean isImageCached(final URL url) {
        return isImageCached(url.toString());
    }

    /**
     * Checks if given image was already cached/stored. File name is
     * based on MD5(url) and its path leads to External Storage Drive
     * cache directory.	Creates path (mkdirs) if necessary.
     *
     * @param sURL - String url of an image
     * @return true if file with such name exists of false otherwise
     */
    public static boolean isImageCached(final String sURL) {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        if (sURL == null || !isExternalStorageAvailable())
            return false;

        File file = getFile(sURL);
        return file.exists();
    }

    /**
     * Generates a temporary file ensuring its path exists (mkdirs)
     * Its path leads to an External Storage Drive if drive is available
     * or using context.getCacheDir() otherwise.
     *
     * @param fileExtension - with or without leading dot. leading dot
     *                      will be added if not provided.
     * @return
     */
    public static File getTempFile(String fileExtension) {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        if (!fileExtension.contains("."))
            fileExtension = "." + fileExtension;
        File file = new File(getCacheDir(), Hash.getMD5("tempfile" + System.currentTimeMillis()) + fileExtension);
        if (!FileUtils.makeDirsForFile(file))
            return null;
        file.deleteOnExit();
        return file;
    }

    /**
     * Generates a temporary file ensuring its path exists (mkdirs)
     * Its path leads to an External Storage Drive if drive is available
     * or using context.getCacheDir() otherwise.
     *
     * @return
     */
    public static File getTempFile() {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        File file = new File(getCacheDir(), Hash.getMD5("tempfile" + System.currentTimeMillis()));
        if (!FileUtils.makeDirsForFile(file))
            return null;
//		final File fileDir = new File(file.getParent());
//		if (fileDir!=null && !fileDir.exists())
//			fileDir.mkdirs();
        return file;
    }

    /**
     * Stores/caches the image on SD card using URL to generate
     * an unique filename based on MD5(URL). Uses getFileForImageCaching(URL)
     *
     * @param sURL
     * @param img
     * @return boolean success
     */
    public static boolean saveImage(final String sURL, final byte[] img) {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        if (sURL == null || img == null || !isWriteable())
            return false;
        final File file = getFile(sURL);
        return FileUtils.makeDirsForFile(file) && FileUtils.byteArrayToFile(img, file);
    }

    /**
     * method returns prepared file name based on MD5 of it's URL
     *
     * @param sURL
     * @return
     */
    private static File getFile(final String sURL) {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        // generate a filename
        final String fileName = Hash.getMD5(sURL);
        final File file = new File(getCacheDir(), fileName);
        if (!FileUtils.makeDirsForFile(file))
            return null;
        return file;
    }

    /**
     * Generates a file to store image preview to. Basically it
     * adds the "/preview" string to the given url and then
     * calls getFileForImageCaching(resulting URL)
     *
     * @param sURL
     * @return File to store image preview to or null if sURL is null
     */
    private static File getPreviewFile(final String sURL) {
        if (TextUtils.isEmpty(sURL))
            return null;
        // generate a filename
        final String fileName = Hash.getMD5(sURL + "/preview");
        final File file = new File(getCacheDir(), fileName);
        if (!FileUtils.makeDirsForFile(file))
            return null;
        return file;
    }

    /**
     * Checks if SD card available
     *
     * @return boolean
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        boolean mExternalStorageAvailable, mExternalStorageWriteable;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if (mExternalStorageAvailable) {
            final File tempFile = new File(sExternalCacheDir, "tempfile.tmp");
            FileUtils.makeDirsForFile(tempFile);
            try {
                mExternalStorageAvailable = tempFile.createNewFile();
                FileUtils.stringToFile(SDCardHelper.class.getName(), tempFile);
                if (!tempFile.delete())
                    tempFile.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
                mExternalStorageAvailable = false;
            }
        }
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    /**
     * Checks permissions and returns true if SD card (External Drive) is available to write to.
     * Applicable for API 23 and higher.
     *
     * @return
     */
    public static boolean isExternalStorageWritePermissionGranted() {
        if (Build.VERSION.SDK_INT > 22) {
            if (sAppContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission is granted");
                return true;
            } else {
                Log.i("Permission is revoked");
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        //else { //permission is automatically granted on sdk<23 upon installation
        Log.i("Permission is granted");
        return true;
    }

    /**
     * Checks permissions and returns true if SD card (External Drive) is available to read from.
     * Applicable for API 23 and higher.
     *
     * @return
     */
    public static boolean isExternalStorageReadPermissionGranted() {
        if (Build.VERSION.SDK_INT > 22) {
            initOnDemand();
            if (sAppContext.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission is granted");
                return true;
            } else {
                Log.i("Permission is revoked");
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        //else { //permission is automatically granted on sdk<23 upon installation
        Log.i("Permission is granted");
        return true;
    }


    /**
     * Checks if SD card available to read from
     *
     * @return boolean
     */
    public static boolean isReadable() {
        return isExternalStorageReadable();
    }

    /**
     * Checks if SD card available to read from
     *
     * @return boolean
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        if (isExternalStorageReadPermissionGranted() && (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
            return true;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            return false;
        }
    }

    /**
     * Checks if SD card available to write to
     *
     * @return boolean
     */
    public static boolean isWriteable() {
        return isExternalStorageWritable();
    }

    /**
     * Checks if SD card available to write to
     *
     * @return boolean
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state) || !isExternalStorageWritePermissionGranted())
            return false;

        // now try to write file physically
        boolean isStorageAvailable;
        final File tempFile = new File(sExternalCacheDir, "tempfile.tmp");
        FileUtils.makeDirsForFile(tempFile);
        try {
            if (!tempFile.createNewFile())
                isStorageAvailable = false;
            else {
                if (!FileUtils.stringToFile(SDCardHelper.class.getName(), tempFile))
                    isStorageAvailable = false;
                else if (!tempFile.delete())
                    tempFile.deleteOnExit();
            }
            isStorageAvailable = true;
        } catch (IOException e) {
            e.printStackTrace();
            isStorageAvailable = false;
        }
        return isStorageAvailable;
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//		    // We can read and write the media
//		    return true;
//		} else {
//		    // Something else is wrong. It may be one of many other states, but all we need
//		    //  to know is we can neither read nor write
//		    return false;
//		}
    }

    /**
     * method used to clear/empty the cache by deleting stored/cached images
     * (basically all the files) in getCacheDir() directory.
     * Does not delete subdirectories and its files
     *
     * @return boolean success
     */
    public static boolean clearImagesCache() {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);

        // depends on isExternalStorageAvailable()
        final File cacheDir = getCacheDir(); //new File(Environment.getExternalStorageDirectory() + SD_CACHE_PATH);

        if (!cacheDir.exists())
            return false;

        FileUtils.deleteFiles(cacheDir);

        return true;
    }

    /**
     * method used to clear/empty the cache by deleting stored/cached files (images)
     * in getCacheDir() directory. Does not delete subdirectories and its files
     *
     * @return boolean success
     */
    public static boolean clearCacheFilesOnly() {
        return clearImagesCache();
    }

    /**
     * method used to clear/empty the cache by deleting all of stored files and dirs
     *
     * @return boolean success
     */
    public static boolean clearCacheFilesAndDirs() {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);

        // depends on isExternalStorageAvailable()
        final File cacheDir = getCacheDir(); //new File(Environment.getExternalStorageDirectory() + SD_CACHE_PATH);

        if (!cacheDir.exists())
            return false;

        FileUtils.deleteFilesAndDirsRecursive(cacheDir);
        return true;
    }

    /**
     * Returns a temporary directory (app cache directory) on External Storage Drive
     * if its available or based on context.getCacheDir() otherwise.
     *
     * @return File or null if its nowhere to write
     */
    public static File getTempDir() {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        return getCacheDir(null);
    }

    /**
     * Returns a temporary directory (app cache directory) on External Storage Drive
     * if its available or based on context.getCacheDir() otherwise
     *
     * @return File or null if its nowhere to write
     */
    public static File getCacheDir() {
        initOnDemand();
        if (!sIsInitialized || SD_CACHE_PATH == null)
            Log.e(SD_CARD_HELPER_INIT_ERR);
        return getCacheDir(null);
    }

    /**
     * Returns a temporary directory (app cache directory) on External Storage Drive
     * if its available or based on context.getCacheDir() otherwise
     *
     * @return File or null if its nowhere to write
     */
    public static File getCacheDir(Context context) {
        if (isExternalStorageAvailable() && isExternalStorageWritable())
            return sExternalCacheDir;
        if (context == null)
            return sInternalCacheDir;
        return context.getCacheDir();
    }

    public static String[] getAPKExpansionFiles(final int mainVersion, final int patchVersion) {
        initOnDemand();
        return getAPKExpansionFiles(sAppContext, mainVersion, patchVersion);
    }

    /**
     * Returns the array of all app expansion files
     * The path is External_Storage_Drive/Android/obb/app_package
     * If mainVersion is given then patchVersion ignored and otherwise.
     *
     * @param context      To get the app package from
     * @param mainVersion  target Main version
     * @param patchVersion target Patch version
     * @return
     */
    public static String[] getAPKExpansionFiles(final Context context, final int mainVersion, final int patchVersion) {

        if (!isExternalStorageAvailable())
            return null;

        final String packageName = context.getPackageName();
        final Vector<String> ret = new Vector<String>();

        // Build the full path to the app's expansion files
        final File root = Environment.getExternalStorageDirectory();
        final File expPath = new File(root.toString() + SD_EXPANSION_FILES_PATH + packageName);

        // Check that expansion file path exists
        if (!expPath.exists())
            return null;

        if (mainVersion > 0) {
            String strMainPath = expPath + File.separator + "main." + mainVersion + "." + packageName + ".obb";
            //String strMainPath = String.format(expPath + File.separator + "patchVersion.%d.%s.obb",mainVersion,packageName);
            File main = new File(strMainPath);
            if (main.isFile()) {
                ret.add(strMainPath);
            }
        }
        if (patchVersion > 0) {
            String strPatchPath = expPath + File.separator + "patch." + patchVersion + "." + packageName + ".obb";
            //String strMainPath = String.format(expPath + File.separator + "patchVersion.%d.%s.obb",mainVersion,packageName);
            File main = new File(strPatchPath);
            if (main.isFile()) {
                ret.add(strPatchPath);
            }
        }

        String[] retArray = new String[ret.size()];
        ret.toArray(retArray);
        return retArray;
    }

    public static File getAPKExpansionFile(int mainVersion) {
        initOnDemand();
        return getAPKExpansionFile(sAppContext, mainVersion);
    }

    /**
     * Returns the expansion file by its mainVersion
     * the path is External_Storage_Drive/Android/obb/app_package
     *
     * @param context     To get the app package from
     * @param mainVersion target Main version
     * @return
     */
    public static File getAPKExpansionFile(Context context, int mainVersion) {
        String packageName = context.getPackageName();

        if (!isExternalStorageAvailable())
            return null;

        // Build the full path to the app's expansion files
        File root = Environment.getExternalStorageDirectory();
        File expPath = new File(root.toString() + SD_EXPANSION_FILES_PATH + packageName);
        if (!expPath.exists())
            return null;

        // Check that expansion file path exists
        if (mainVersion > 0) {
            String strMainPath = expPath + File.separator + "main." + mainVersion + "." + packageName + ".obb";
            //String strMainPath = String.format(expPath + File.separator + "main.%d.%s.obb",mainVersion,packageName);
            File main = new File(strMainPath);
            if (main.isFile()) {
                return main;
            }
        }

        return null;
    }

    /**
     * Returns a set of found external paths
     *
     * @return
     */
    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1)
                s = s + new String(buffer);
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

    /**
     * Returns all available SD-Cards in the system (including emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static HashSet<String> getStorageDirectories() {
        // Final set of paths
        final HashSet<String> storageDirs = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                storageDirs.add("/storage/sdcard0");
            } else {
                storageDirs.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (DeviceInfo.hasAPI(18)) {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final Pattern separatorPattern = Pattern.compile(File.separator);
                final String[] folders = separatorPattern.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            } else {
                rawUserId = "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                storageDirs.add(rawEmulatedStorageTarget);
            } else {
                storageDirs.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(storageDirs, rawSecondaryStorages);
        }
        return storageDirs; //.toArray(new String[storageDirs.size()]);
    }

    private static void initOnDemand() {
        // init on demand
        if (sAppContext == null && Initializer.getsAppContext() != null) {
            init(Initializer.getsAppContext());
        }
    }

}
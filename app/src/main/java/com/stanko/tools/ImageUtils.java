package com.stanko.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.Math.max;

/**
 * class used to store/restore of images
 *
 * @author Stan Koshutsky
 */
public class ImageUtils {

    /**
     * Returns instance of BitmapInfo for given image File
     *
     * @param bitmapFile - am image File
     * @return BitmapInfo or null if File is null or not readable
     */
    public static BitmapInfo getBitmapInfoFromFile(final File bitmapFile) {
        if (!FileUtils.isReadable(bitmapFile) || bitmapFile != null && bitmapFile.length() < 32)
            return null;

        BitmapInfo bitmapInfo = null;
        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        // decode image size
        BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inJustDecodeBounds = true;
        try {
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }
            if (fileDescriptor != null)
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
            else
                BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);
            bitmapInfo = new BitmapInfo(bmfOptions.outWidth, bmfOptions.outHeight);
        } catch (FileNotFoundException e) {
            Log.e("ImageUtils", e);
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }
        return bitmapInfo;
    }

    /**
     * Returns instance of BitmapInfo for given image File stream
     *
     * @param inputStream
     * @return BitmapInfo or null if stream is null or any Exception happens
     */
    public static BitmapInfo getBitmapInfoFromStream(final InputStream inputStream) {
        if (inputStream == null)
            return null;

        BitmapInfo bitmapInfo = null;

        final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inJustDecodeBounds = true;
        try {
            // decode image size
            BitmapFactory.decodeStream(inputStream, null, bmfOptions);
            bitmapInfo = new BitmapInfo(bmfOptions.outWidth, bmfOptions.outHeight);
        } catch (Exception e) {
            Log.e(e);
        }
        return bitmapInfo;
    }

    /**
     * Returns instance of BitmapInfo for given image Uri, needs Context so it depends to
     * Initializer's one so it has to be initialized first.
     *
     * @param uri of an image
     * @return BitmapInfo or null if Uri or Context is null (Initializer was not initialized)
     */
    public static BitmapInfo getBitmapInfoFromUri(final Uri uri) {
        final Context context = Initializer.getsAppContext();
        return getBitmapInfoFromUri(uri, context);
    }

    /**
     * Returns Uri of given drawable resource. It needs Context so it depends to Initializer's one
     * and thus Initializer has to be initialized first.
     *
     * @param bitmapResId
     * @return BitmapInfo or null if bitmapResId==0 or Initializer was not initialized
     */
    public static BitmapInfo getBitmapInfoFromResources(final int bitmapResId) {
        return getBitmapInfoFromResources(Initializer.getsAppContext(), bitmapResId);
    }

    /**
     * Returns Uri of given drawable resource. It needs Context so it depends to Initializer's one
     *
     * @param context
     * @param bitmapResId
     * @return BitmapInfo or null if context is null or bitmapResId==0
     */
    public static BitmapInfo getBitmapInfoFromResources(final Context context, final int bitmapResId) {
        if (context == null || bitmapResId == 0)
            return null;
        final Uri uri = getUriOfBitmapFromResources(context, bitmapResId);
        return getBitmapInfoFromUri(uri, context);
    }

    /**
     * Returns instance of BitmapInfo for given image Uri
     *
     * @param uri     of an image
     * @param context any Context
     * @return BitmapInfo or null if Uri or Context is null
     */
    public static BitmapInfo getBitmapInfoFromUri(final Uri uri, final Context context) {
        if (context == null || uri == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;
        InputStream inputStream = null;
        BitmapInfo bitmapInfo = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            bitmapInfo = getBitmapInfoFromStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }
        return bitmapInfo;
    }

    /**
     * Class represents generic Bitmap information like heght, width and orientation (ignoring EXIF)
     * Since all fields are final and public no getters need.
     */
    public static class BitmapInfo {

        public final int height;
        public final int width;
        public final boolean hasPortraitOrientation;
        public final boolean hasLandscapeOrientation;
        public final boolean hasSquareForm;
        public final long sizeInBytes;

        public BitmapInfo(final int width, final int height) {
            this.height = height;
            this.width = width;
            hasPortraitOrientation = height > width;
            hasLandscapeOrientation = width > height;
            hasSquareForm = width == height;
            sizeInBytes = (long) (height * width);
        }

        public BitmapInfo(final Bitmap bitmap) {
            this.height = bitmap.getHeight();
            this.width = bitmap.getWidth();
            hasPortraitOrientation = height > width;
            hasLandscapeOrientation = width > height;
            hasSquareForm = width == height;
            sizeInBytes = getBitmapSizeInBytes(bitmap);
        }

        /**
         * Returns height in px corresponding to given width regarding image aspect ratio
         *
         * @param newWidth
         * @return
         */
        public float getHeightByWidth(final float newWidth) {
            return (newWidth * height / width);
        }

        /**
         * Returns width in px corresponding to given height regarding image aspect ratio
         *
         * @param newHeight
         * @return
         */
        public float getWidthByHeight(final float newHeight) {
            return (newHeight * width / height);
        }

        @Override
        public String toString() {
            return "height: " + height + " width: " + width + " hasLandscapeOrientation: " + hasLandscapeOrientation + " hasPortraitOrientation: " + hasPortraitOrientation + " " + " hasSquareForm: " + hasSquareForm;
        }
    }

    @SuppressLint("NewApi")
    /**
     * Returns size of a bitmap in bytes
     *
     * @param bitmap
     * @return
     */
    public static int getBitmapSizeInBytes(final Bitmap bitmap) {
        if (bitmap == null)
            return 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getRowBytes() * bitmap.getHeight();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getAllocationByteCount();
        }
    }

    //*********************************************************************************************
    //
    // Bitmap getter methods
    //
    //*********************************************************************************************

    //
    // File
    //

    /**
     * Decodes an image from File to Bitmap without any scaling
     * Uses default sideSizeLimit (0) and isOptimistic(false)
     *
     * @param bitmapFile
     * @return
     */
    public static Bitmap getBitmapFromFile(final String bitmapFile) {
        return getBitmapFromFile(new File(bitmapFile));
    }

    /**
     * Decodes an image from File to Bitmap without any scaling
     *
     * @param bitmapFile
     * @return
     */
    public static Bitmap getBitmapFromFile(final File bitmapFile) {
        if (bitmapFile == null || bitmapFile.length() == 0 || !FileUtils.isReadable(bitmapFile))
            return null;

        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        // decode image size
        final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT < 21)
            bmfOptions.inPurgeable = true;
        try {
            // decode with inSampleSize
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
            else
                return BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);
        } catch (FileNotFoundException e) {
            Log.e(e);
        } catch (Throwable e) {
            Log.e(e);
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }
        return null;
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     * Uses default isOptimistic = false
     * if heightLimit = 0 it will be set to DeviceInfo.getDeviceMaxSideSizeByDensity()
     *
     * @param bitmapFile
     * @param heightLimit - max image height.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxHeight(final String bitmapFile, final int heightLimit) {
        return getBitmapFromFileWithMaxHeight(bitmapFile, heightLimit, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     * Uses default isOptimistic(false)
     * if heightLimit = 0 it will be set to DeviceInfo.getDeviceMaxSideSizeByDensity()
     *
     * @param bitmapFile
     * @param heightLimit - max image height.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxHeight(final File bitmapFile, final int heightLimit) {
        return getBitmapFromFileWithMaxHeight(bitmapFile, heightLimit, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     * if heightLimit = 0 it will be set to DeviceInfo.getDeviceMaxSideSizeByDensity()
     *
     * @param bitmapFile
     * @param heightLimit  - max image height.
     * @param isOptimistic - if true may return bigger image, smaller or equal if false
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxHeight(final String bitmapFile, final int heightLimit, final boolean isOptimistic) {
        if (bitmapFile == null || bitmapFile.length() == 0)
            return null;
        return getBitmapFromFileWithMaxHeight(new File(bitmapFile), heightLimit, isOptimistic);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFile
     * @param heightLimit  - max image height. Uses DeviceInfo height if 0 passed.
     * @param isOptimistic - if true may return bigger image, smaller or equal if false
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxHeight(final File bitmapFile, final int heightLimit, final boolean isOptimistic) {
        return getBitmapFromFileWithMaxSideSize(bitmapFile, heightLimit, true, isOptimistic);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption
     * Uses default widthLimit = 0  and isOptimistic = false
     *
     * @param bitmapFile
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxWidth(final String bitmapFile) {
        return getBitmapFromFileWithMaxWidth(new File(bitmapFile), 0, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption
     * Uses default widthLimit = 0 and isOptimistic = false
     *
     * @param bitmapFile
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxWidth(final File bitmapFile) {
        return getBitmapFromFileWithMaxWidth(bitmapFile, 0, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     * Uses default isOptimistic = false
     *
     * @param bitmapFile
     * @param widthLimit - max image width. Uses DeviceInfo width if 0 passed.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxWidth(final String bitmapFile, final int widthLimit) {
        return getBitmapFromFileWithMaxWidth(bitmapFile, widthLimit, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     * Uses default isOptimistic(false)
     *
     * @param bitmapFile
     * @param widthLimit - max image width. Uses DeviceInfo width if 0 passed.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxWidth(final File bitmapFile, final int widthLimit) {
        return getBitmapFromFileWithMaxWidth(bitmapFile, widthLimit, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFile
     * @param widthLimit   - max image width. Uses DeviceInfo width if 0 passed.
     * @param isOptimistic - if true may return bigger image, smaller or equal if false
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxWidth(final String bitmapFile, final int widthLimit, final boolean isOptimistic) {
        if (bitmapFile == null || bitmapFile.length() == 0)
            return null;
        return getBitmapFromFileWithMaxWidth(new File(bitmapFile), widthLimit, isOptimistic);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFile
     * @param widthLimit   - max image width. Uses DeviceInfo width if 0 passed.
     * @param isOptimistic - if true may return bigger image, smaller or equal if false
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxWidth(final File bitmapFile, final int widthLimit, final boolean isOptimistic) {
        return getBitmapFromFileWithMaxSideSize(bitmapFile, widthLimit, false, isOptimistic);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFilePath
     * @param maxSideSize    - max image side size (height or width). Uses DeviceInfo width if 0 passed.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxSideSize(final String bitmapFilePath, final int maxSideSize) {
        return getBitmapFromFileWithMaxSideSize(new File(bitmapFilePath), maxSideSize, null, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFile
     * @param maxSideSize - max image side size (height or width). Uses DeviceInfo width if 0 passed.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxSideSize(final File bitmapFile, final int maxSideSize, final boolean isOptimistic) {
        return getBitmapFromFileWithMaxSideSize(bitmapFile, maxSideSize, null, isOptimistic);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFilePath
     * @param maxSideSize    - max image side size (height or width). Uses DeviceInfo width if 0 passed.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxSideSize(final String bitmapFilePath, final int maxSideSize, final boolean isOptimistic) {
        return getBitmapFromFileWithMaxSideSize(new File(bitmapFilePath), maxSideSize, null, isOptimistic);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFile
     * @param maxSideSize - max image side size (height or width). Uses DeviceInfo width if 0 passed.
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxSideSize(final File bitmapFile, final int maxSideSize) {
        return getBitmapFromFileWithMaxSideSize(bitmapFile, maxSideSize, null, false);
    }

    /**
     * Decodes an image to Bitmap downscaling it to reduce memory consumption.
     *
     * @param bitmapFile
     * @param maxSideSize  - max image side size. Uses DeviceInfo width if 0 passed.
     * @param isOptimistic - if true may return bigger image, smaller or equal if false
     * @return
     */
    public static Bitmap getBitmapFromFileWithMaxSideSize(final File bitmapFile,
                                                          int maxSideSize,
                                                          final Boolean isByHeight,
                                                          final boolean isOptimistic) {
        if (bitmapFile == null || bitmapFile.length() == 0 || !FileUtils.isReadable(bitmapFile))
            return null;

        if (maxSideSize == 0) {
            maxSideSize = DeviceInfo.getDeviceMaxSideSizeByDensity();
        }

        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        // decode image size
        final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inJustDecodeBounds = true;
        if (Build.VERSION.SDK_INT < 21)
            bmfOptions.inPurgeable = true;
        try {
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
            else
                BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);

            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ignored) {
                }

            // Find the correct scale value. It should be the power of 2.
            bmfOptions.inSampleSize = getScaleRatio(bmfOptions, maxSideSize, isByHeight, isOptimistic);
            bmfOptions.inJustDecodeBounds = false;

            // decode with inSampleSize
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
            else
                return BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);
        } catch (FileNotFoundException e) {
            Log.e("ImageUtils", e);
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }
        return null;
    }

    public static Bitmap getResizedBitmapFromFile(final String bitmapFile, final int newHeight, final int newWidth) {
        return getResizedBitmapFromFile(new File(bitmapFile), newHeight, newWidth, true);
    }

    public static Bitmap getResizedBitmapFromFile(final File bitmapFile, final int newHeight, final int newWidth) {
        return getResizedBitmapFromFile(bitmapFile, newHeight, newWidth, true);
    }

    public static Bitmap getResizedBitmapFromFile(final String bitmapFile, final int newHeight, final int newWidth, final boolean isOptimistic) {
        return getResizedBitmapFromFile(new File(bitmapFile), newHeight, newWidth, isOptimistic);
    }

    public static Bitmap getResizedBitmapFromFile(final File bitmapFile, final int newHeight, int newWidth, final boolean isOptimistic) {
        if (!FileUtils.isReadable(bitmapFile))
            return null;

        final Bitmap bitmap = getBitmapFromFileWithMaxSideSize(bitmapFile, Math.max(newHeight, newWidth), isOptimistic);
        if (newHeight == 0 || newWidth == 0) {
            final BitmapInfo bitmapInfo = new BitmapInfo(bitmap);
            if (newHeight == 0)
                newWidth = (int) bitmapInfo.getWidthByHeight(newHeight);
            else
                newWidth = (int) bitmapInfo.getHeightByWidth(newWidth);
        }

        return getResizedBitmap(bitmap, newHeight, newWidth);
    }

    // FileDescriptor

    public static Bitmap getBitmapFromFileDescriptorWithMaxHeight(final FileDescriptor fileDescriptor, final int maxHeight) {
        return getBitmapFromFileDescriptorWithMaxSideSize(fileDescriptor, maxHeight, true, false);
    }

    public static Bitmap getBitmapFromFileDescriptorWithMaxHeight(final FileDescriptor fileDescriptor, final int maxHeight, final boolean isOptimistic) {
        return getBitmapFromFileDescriptorWithMaxSideSize(fileDescriptor, maxHeight, true, isOptimistic);
    }

    public static Bitmap getBitmapFromFileDescriptorWithMaxWidth(final FileDescriptor fileDescriptor, final int maxWidth) {
        return getBitmapFromFileDescriptorWithMaxSideSize(fileDescriptor, maxWidth, false, false);
    }

    public static Bitmap getBitmapFromFileDescriptorWithMaxWidth(final FileDescriptor fileDescriptor, final int maxWidth, final boolean isOptimistic) {
        return getBitmapFromFileDescriptorWithMaxSideSize(fileDescriptor, maxWidth, false, isOptimistic);
    }

    public static Bitmap getBitmapFromFileDescriptorWithMaxSideSize(final FileDescriptor fileDescriptor, final int maxSideSize) {
        return getBitmapFromFileDescriptorWithMaxSideSize(fileDescriptor, maxSideSize, null, false);
    }

    public static Bitmap getBitmapFromFileDescriptorWithMaxSideSize(final FileDescriptor fileDescriptor, final int maxSideSize, final boolean isOptimistic) {
        return getBitmapFromFileDescriptorWithMaxSideSize(fileDescriptor, maxSideSize, null, isOptimistic);
    }

    public static Bitmap getBitmapFromFileDescriptorWithMaxSideSize(final FileDescriptor fileDescriptor,
                                                                    final int maxSideSize,
                                                                    final Boolean isByHeight,
                                                                    final boolean isOptimistic) {
        if (maxSideSize == 0)
            return null;

        // decode image size
        final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inJustDecodeBounds = true;
        if (Build.VERSION.SDK_INT < 21)
            bmfOptions.inPurgeable = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);

        // Find the correct scale value. It should be the power of 2.
        bmfOptions.inSampleSize = getScaleRatio(bmfOptions, maxSideSize, isByHeight, isOptimistic);
        bmfOptions.inJustDecodeBounds = false;

        // decode with inSampleSize
        final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
        return bitmap;
    }


    //
    // Resources
    //

    public static Bitmap getBitmapFromResources(final int drawableResId) {
        return getBitmapFromResources(Initializer.getResources(), drawableResId);
    }

    public static Bitmap getBitmapFromResources(final Context context, final int drawableResId) {
        if (context == null || drawableResId == 0)
            return null;

        return getBitmapFromResources(context.getResources(), drawableResId);
    }

    public static Bitmap getBitmapFromResources(final Resources resources, final int drawableResId) {
        if (resources == null || drawableResId == 0)
            return null;

        return BitmapFactory.decodeResource(resources, drawableResId);
    }

    public static Bitmap getBitmapFromResourcesWithMaxHeight(final Context context, final int drawableResId, final int maxHeight) {
        if (context == null || drawableResId == 0)
            return null;
        return getBitmapFromResourcesWithMaxHeight(context, drawableResId, maxHeight, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxHeight(final Context context, final int drawableResId, final int maxHeight, final boolean isOptimistic) {
        if (context == null || drawableResId == 0 || maxHeight == 0)
            return null;
        return getBitmapFromResourcesWithMaxSideSize(context, drawableResId, maxHeight, true, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxWidth(final Context context, final int drawableResId, final int maxWidth) {
        if (context == null || drawableResId == 0)
            return null;
        return getBitmapFromResourcesWithMaxWidth(context, drawableResId, maxWidth, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxWidth(final Context context, final int drawableResId, final int maxWidth, final boolean isOptimistic) {
        if (context == null || drawableResId == 0 || maxWidth == 0)
            return null;
        return getBitmapFromResourcesWithMaxSideSize(context, drawableResId, maxWidth, false, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxSideSize(final Context context, final int drawableResId, final int maxSideSize) {
        if (context == null || drawableResId == 0)
            return null;
        return getBitmapFromResourcesWithMaxSideSize(context, drawableResId, maxSideSize, null, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxSideSize(final Context context,
                                                               final int drawableResId,
                                                               final int maxSideSize,
                                                               final Boolean isByHeight,
                                                               final boolean isOptimistic) {
        if (maxSideSize == 0)
            return null;
        final Resources resources = context.getResources();
        final Uri uri = getUriOfBitmapFromResources(resources, drawableResId);
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            if (Build.VERSION.SDK_INT < 21)
                bmfOptions.inPurgeable = true;
            bmfOptions.inJustDecodeBounds = true;
            final ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver == null)
                return null;
            inputStream = contentResolver.openInputStream(uri);
            // decode image size
            BitmapFactory.decodeStream(inputStream, null, bmfOptions);

            // Find the correct scale value. It should be the power of 2.
            bmfOptions.inSampleSize = getScaleRatio(bmfOptions, maxSideSize, isByHeight, isOptimistic);
            bmfOptions.inJustDecodeBounds = false;

            // decode with inSampleSize
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOptions);

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }

        return bitmap;
    }

    public static Bitmap getBitmapFromStreamWithMaxSideSize(final InputStream inputStream,
                                                            final int maxSideSize,
                                                            final Boolean isByHeight,
                                                            final boolean isOptimistic) {
        if (maxSideSize == 0)
            return null;

        // decode image size
        final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
        bmfOptions.inJustDecodeBounds = true;
        if (Build.VERSION.SDK_INT < 21)
            bmfOptions.inPurgeable = true;
        BitmapFactory.decodeStream(inputStream, null, bmfOptions);

        // Find the correct scale value. It should be the power of 2.
        bmfOptions.inSampleSize = getScaleRatio(bmfOptions, maxSideSize, isByHeight, isOptimistic);
        bmfOptions.inJustDecodeBounds = false;

        // decode with inSampleSize
        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOptions);
        return bitmap;
    }

    /**
     * Returns Bitmap obtained from Raw resources folder, uses Inititalizer context so Initializer
     * must be initialized before call.
     *
     * @param mBitmapResId
     * @return
     */
    public static Bitmap getBitmapFromRawResources(final int mBitmapResId) {
        return getBitmapFromRawResources(Initializer.getsAppContext(), mBitmapResId);
    }

    /**
     * Returns @Bitmap obtained from Raw resources folder
     *
     * @param context
     * @param mBitmapResId
     * @return
     */
    public static Bitmap getBitmapFromRawResources(final Context context, final int mBitmapResId) {
        if (context == null || mBitmapResId == 0)
            return null;
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getResources().openRawResource(mBitmapResId);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }

        return bitmap;
    }

    /**
     * Returns Bitmap resized to newHeight and newWidth taken from Drawable resources.
     * Ignores image aspect ratio if both dimensions set, respects aspect ratio if only one
     * dimension (height or width) set but other is 0. Uses Initializer's context so Initializer
     * must be initialized previously otherwise returns null.
     * Returns null if Initializer has not been initialized before or bitmapResId is 0 or one
     * dimensions is lower than 0 or both dimensions are set to 0 or Exception (like OOM) happens.
     *
     * @param bitmapResId
     * @param newHeight
     * @param newWidth
     * @return resized Bitmap or null
     */
    public static Bitmap getResizedBitmapFromResources(final int bitmapResId, final int newHeight, final int newWidth) {
        if (bitmapResId == 0)
            return null;
        return getResizedBitmapFromResources(Initializer.getsAppContext(), bitmapResId, newHeight, newWidth, false);
    }

    /**
     * Returns Bitmap resized to newHeight and newWidth taken from Drawable resources.
     * Ignores image aspect ratio if both dimensions set, respects aspect ratio if only one
     * dimension (height or width) set but other is 0.
     * Returns null if context is null or bitmapResId is 0 or one dimensions is lower than 0 or both
     * dimensions are set to 0 or Exception (like OOM) happens.
     *
     * @param context
     * @param bitmapResId
     * @param newHeight
     * @param newWidth
     * @return resized Bitmap or null
     */
    public static Bitmap getResizedBitmapFromResources(final Context context, final int bitmapResId, final int newHeight, final int newWidth) {
        if (context == null || bitmapResId == 0)
            return null;
        return getResizedBitmapFromResources(context, bitmapResId, newHeight, newWidth, false);
    }

    /**
     * Returns Bitmap resized to newHeight and newWidth taken from Drawable resources.
     * Ignores image aspect ratio if both dimensions set, respects aspect ratio if only one
     * dimension (height or width) set but other is 0
     * Returns null if context is null or bitmapResId is 0 or one dimensions is lower than 0 or both
     * dimensions are set to 0 or Exception (like OOM) happens.
     *
     * @param context
     * @param bitmapResId
     * @param newHeight
     * @param newWidth
     * @param isOptimistic
     * @return resized Bitmap or null
     */
    public static Bitmap getResizedBitmapFromResources(final Context context,
                                                       final int bitmapResId,
                                                       final int newHeight,
                                                       final int newWidth,
                                                       final boolean isOptimistic) {
        if (context == null || bitmapResId == 0)
            return null;
        final Bitmap bitmap = getBitmapFromResourcesWithMaxSideSize(context, bitmapResId, max(newHeight, newWidth), null, isOptimistic);
        return getResizedBitmap(bitmap, newHeight, newWidth);
    }

    //
    // from Uri
    //
    public static Uri getUriOfBitmapFromResources(final int mBitmapResId) {
        return getUriOfBitmapFromResources(Initializer.getsAppContext(), mBitmapResId);
    }

    public static Uri getUriOfBitmapFromResources(final Context context, final int mBitmapResId) {
        if (context == null || mBitmapResId == 0)
            return null;
        Resources resources = context.getResources();
        return getUriOfBitmapFromResources(resources, mBitmapResId);
    }

    public static Uri getUriOfBitmapFromResources(final Resources resources, final int mBitmapResId) {
        if (resources == null || mBitmapResId == 0)
            return null;
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(mBitmapResId) + '/' +
                resources.getResourceTypeName(mBitmapResId) + '/' +
                resources.getResourceEntryName(mBitmapResId));
    }

    public static Bitmap getBitmapFromUri(final Context context, final Uri uri) {
        if (context == null || uri == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            if (Build.VERSION.SDK_INT < 21)
                bmfOptions.inPurgeable = true;
            bmfOptions.inJustDecodeBounds = false;
            inputStream = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOptions);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }

        return bitmap;
    }

    public static Bitmap getBitmapFromUriWithMaxSideSize(final Uri uri, final int sideSizeLimit) {
        return getBitmapFromUriWithMaxSideSize(Initializer.getsAppContext(), uri, sideSizeLimit, false);
    }

    public static Bitmap getBitmapFromUriWithMaxSideSize(final Context context, final Uri uri, final int sideSizeLimit) {
        if (context == null || uri == null)
            return null;
        return getBitmapFromUriWithMaxSideSize(context, uri, sideSizeLimit, false);
    }

    public static Bitmap getBitmapFromUriWithMaxSideSize(final Context context, final Uri uri, final int sideSizeLimit, final boolean isOptimistic) {
        if (context == null || uri == null)
            return null;

        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;

        Bitmap bitmap = null;
        InputStream inputStream = null;

        int maxWidth, maxHeight;
        if (sideSizeLimit > 0) {
            maxWidth = sideSizeLimit;
            maxHeight = sideSizeLimit;
        } else {
            maxWidth = DeviceInfo.getDeviceMaxSideSizeByDensity();
            maxHeight = maxWidth;
        }
        try {
            inputStream = contentResolver.openInputStream(uri);

            // decode image size
            final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            bmfOptions.inJustDecodeBounds = true;
            if (Build.VERSION.SDK_INT < 21)
                bmfOptions.inPurgeable = true;
            BitmapFactory.decodeStream(inputStream, null, bmfOptions);

            // Find the correct scale value. It should be the power of 2.
            bmfOptions.inSampleSize = getScaleRatio(bmfOptions, sideSizeLimit, null, isOptimistic);
            bmfOptions.inJustDecodeBounds = false;

            // decode with inSampleSize
            // BitmapFactory.Options o2 = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOptions);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }
        return bitmap;
    }

    // height limit
    public static Bitmap getBitmapFromUriWithMaxHeight(final Uri uri, final int heightLimit) {
        return getBitmapFromUriWithMaxHeight(Initializer.getsAppContext(), uri, heightLimit, false);
    }

    public static Bitmap getBitmapFromUriWithMaxHeight(final Context context, final Uri uri, final int heightLimit) {
        if (context == null || uri == null)
            return null;
        return getBitmapFromUriWithMaxHeight(context, uri, heightLimit, false);
    }

    public static Bitmap getBitmapFromUriWithMaxHeight(final Context context, final Uri uri, final int heightLimit, final boolean isOptimistic) {
        if (context == null || uri == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;

        Bitmap bitmap = null;
        InputStream inputStream = null;

        int maxWidth, maxHeight;
        if (heightLimit > 0) {
            maxHeight = heightLimit;
        } else {
            maxHeight = DeviceInfo.getDeviceMaxSideSizeByDensity();
        }
        try {
            inputStream = contentResolver.openInputStream(uri);

            // decode image size
            final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            bmfOptions.inJustDecodeBounds = true;
            if (Build.VERSION.SDK_INT < 21)
                bmfOptions.inPurgeable = true;
            BitmapFactory.decodeStream(inputStream, null, bmfOptions);

            // Find the correct scale value. It should be the power of 2.
            bmfOptions.inSampleSize = getScaleRatio(bmfOptions, heightLimit, true, isOptimistic);
            bmfOptions.inJustDecodeBounds = false;

            // decode with inSampleSize
            // BitmapFactory.Options o2 = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOptions);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }
        return bitmap;
    }

    // width limit
    public static Bitmap getBitmapFromUriWithMaxWidth(final Uri uri, final int widthLimit) {
        return getBitmapFromUriWithMaxWidth(Initializer.getsAppContext(), uri, widthLimit, false);
    }

    public static Bitmap getBitmapFromUriWithMaxWidth(final Context context, final Uri uri, final int widthLimit) {
        if (context == null || uri == null)
            return null;
        return getBitmapFromUriWithMaxWidth(context, uri, widthLimit, false);
    }

    public static Bitmap getBitmapFromUriWithMaxWidth(final Context context, final Uri uri, final int widthLimit, final boolean isOptimistic) {
        if (context == null || uri == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;

        Bitmap bitmap = null;
        InputStream inputStream = null;

        int maxWidth;
        if (widthLimit > 0) {
            maxWidth = widthLimit;
        } else {
            maxWidth = DeviceInfo.getDeviceMaxSideSizeByDensity();
        }
        try {
            inputStream = contentResolver.openInputStream(uri);
            // decode image size
            final BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            bmfOptions.inJustDecodeBounds = true;
            if (Build.VERSION.SDK_INT < 21)
                bmfOptions.inPurgeable = true;
            BitmapFactory.decodeStream(inputStream, null, bmfOptions);
            // Find the correct scale value. It should be the power of 2.
            bmfOptions.inSampleSize = getScaleRatio(bmfOptions, widthLimit, false, isOptimistic);
            bmfOptions.inJustDecodeBounds = false;
            // decode with inSampleSize
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOptions);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
        }
        return bitmap;
    }

    private static int getScaleRatio(final BitmapFactory.Options bmfOptions, final int maxSideSize, final Boolean isByHeight, final boolean isOptimistic) {
        // Find the correct scale value. It should be the power of 2.
        int width_tmp = bmfOptions.outWidth;
        int height_tmp = bmfOptions.outHeight;
        int scale = 1;
        if (isByHeight == null) {
            if (height_tmp > width_tmp)
                while (height_tmp > maxSideSize && (!isOptimistic || height_tmp / maxSideSize > 1.5f)) {
                    height_tmp >>= 1;
                    scale++;
                }
            else
                while (width_tmp > maxSideSize && (!isOptimistic || width_tmp / maxSideSize > 1.5f)) {
                    width_tmp >>= 1;
                    scale++;
                }

        } else if (isByHeight)
            while (height_tmp > maxSideSize && (!isOptimistic || height_tmp / maxSideSize > 1.5f)) {
                height_tmp >>= 1;
                scale++;
            }
        else
            while (width_tmp > maxSideSize && (!isOptimistic || width_tmp / maxSideSize > 1.5f)) {
                width_tmp >>= 1;
                scale++;
            }
        return isOptimistic && scale > 1 ? scale - 1 : scale;

    }


    //*********************************************************************************************
    //
    // Image manipulating methods (EXIF)
    //
    //*********************************************************************************************

    /**
     * Rotates given image corresponding to EXIF.
     * Warning! Samsung and LG cameras has an EXIF bug so result may be wrong.
     * Also could produce OOM which will be caught, false will be returned in such case.
     *
     * @param targetFilePath
     * @return true if succeed and no Exception happens
     */
    public static boolean rotateBitmapByExifAndSave(final String targetFilePath) {
        return !TextUtils.isEmpty(targetFilePath) && rotateBitmapByExifAndSave(new File(targetFilePath));
    }

    /**
     * Rotates given image corresponding to EXIF.
     * Warning! Samsung and LG cameras has an EXIF bug so result may be wrong.
     * Also could produce OOM which will be caught, false will be returned in such case.
     *
     * @param targetFile
     * @return true if succeed and no Exception happens
     */
    public static boolean rotateBitmapByExifAndSave(final File targetFile) {

        if (!FileUtils.isWritable(targetFile) && targetFile.length() > 0)
            return false;

        boolean isSucceed;
        // detecting if an image needs to be rotated
        try {
            final Matrix matrix = new Matrix();
            final int rotateAngle = getExifRotateAngle(targetFile);
            boolean isRotationNeeded = rotateAngle > 0;
            if (isRotationNeeded) {
                matrix.postRotate(rotateAngle);
            }
            isSucceed = !isRotationNeeded;

            if (isRotationNeeded) {
                BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
                if (Build.VERSION.SDK_INT < 21)
                    bmfOptions.inPurgeable = true;
                Bitmap bitmap = null;
                FileInputStream fileInputStream = null;
                FileDescriptor fileDescriptor = null;
                try {
                    fileInputStream = new FileInputStream(targetFile);
                    try {
                        fileDescriptor = fileInputStream.getFD();
                    } catch (IOException ignored) {
                    }

                    if (fileDescriptor != null)
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
                    else
                        bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);

                } catch (FileNotFoundException e) {
                    isSucceed = false;
                } finally {
                    if (fileInputStream != null)
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                }
                if (bitmap != null) {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    isSucceed = ImageUtils.saveBitmapToJPEGFile(bitmap, targetFile, 99);
                    bitmap.recycle();
                }
            }

        } catch (Exception e) {
            isSucceed = false;
            Log.e(e);
        } catch (Throwable e) {
            // Out of stupid vedroid's memory
            isSucceed = false;
            Log.e(e);
        }

        return isSucceed;
    }

    /**
     * Detects and returns image rotate angle bty its EXIF information.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid.
     *
     * @param targetFile
     * @return
     */
    public static int getExifRotateAngle(final File targetFile) {
        if (!FileUtils.isReadable(targetFile)) {
            Log.e("File is not readable! " + targetFile);
            return 0;
        }

        try {
            final ExifInterface exifReader = new ExifInterface(targetFile.getAbsolutePath());
            final int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            return getExifRotateAngle(orientation);
        } catch (Exception e) {
            // like there is no EXIF support?
            Log.e(e);
        }

        return 0;
    }

    /**
     * Detects and returns image rotate angle bty its EXIF information.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid. This
     * method has a workaround which may help.
     *
     * @param context
     * @param uri
     * @return
     */
    public static float getExifRotateAngle(final Context context, final Uri uri) {
        try {
            if (uri.getScheme().equals("content")) {
                //From the media gallery
                String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
                final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int value = 0;
                if (cursor != null) {
                    if (cursor.moveToFirst())
                        value = cursor.getInt(0);
                    cursor.close();
                }
                return value;
            } else if (uri.getScheme().equals("file")) {
                //From a file saved by the camera
                final ExifInterface exifReader = new ExifInterface(uri.getPath());
                final int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                final int rotateAngle = getExifRotateAngle(orientation);
                return rotateAngle;
            }
            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Returns rotate angle in degrees according to ExifInterface.ORIENTATION
     *
     * @param orientation
     * @return
     */
    private static int getExifRotateAngle(final int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;

            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;

            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;

            default: // ExifInterface.ORIENTATION_NORMAL
                return 0;
        }
    }

    /**
     * Returns Bitmap rotated according to EXIF.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid.
     *
     * @param targetFilePath
     * @return Bitmap or null if targetFilePath is null or OOM/other Exception happens
     */
    public static Bitmap getRotatedBitmapByExif(final String targetFilePath) {
        if (targetFilePath == null || targetFilePath.length() == 0) {
            Log.e("File is not readable! " + targetFilePath);
            return null;
        }
        return getRotatedBitmapByExif(new File(targetFilePath));
    }

    /**
     * Returns Bitmap rotated according to EXIF.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid.
     *
     * @param targetFile
     * @return Bitmap or null if targetFile is null/not readable or OOM/other Exception happens
     */
    public static Bitmap getRotatedBitmapByExif(final File targetFile) {

        if (!FileUtils.isReadable(targetFile)) {
            Log.e("File is not readable! " + targetFile);
            return null;
        }

        Bitmap bitmap = null;
        // detecting if an image needs to be rotated
        try {
            final Matrix matrix = new Matrix();
            final int rotateAngle = getExifRotateAngle(targetFile);
            boolean isRotationNeeded = rotateAngle > 0;
            if (isRotationNeeded) {
                matrix.postRotate(rotateAngle);
            }

            BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            if (Build.VERSION.SDK_INT < 21)
                bmfOptions.inPurgeable = true;

            if (isRotationNeeded) {
                FileInputStream fileInputStream = null;
                FileDescriptor fileDescriptor = null;
                try {
                    fileInputStream = new FileInputStream(targetFile);
                    try {
                        fileDescriptor = fileInputStream.getFD();
                    } catch (IOException ignored) {
                    }

                    if (fileDescriptor != null)
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
                    else
                        bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);
                } catch (FileNotFoundException e) {
                } finally {
                    if (fileInputStream != null)
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                }
                if (bitmap != null)
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } else {
                FileInputStream fileInputStream = null;
                FileDescriptor fileDescriptor = null;
                try {
                    fileInputStream = new FileInputStream(targetFile);
                    try {
                        fileDescriptor = fileInputStream.getFD();
                    } catch (IOException ignored) {
                    }

                    if (fileDescriptor != null)
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOptions);
                    else
                        bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOptions);
                } catch (FileNotFoundException e) {
                } finally {
                    if (fileInputStream != null)
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                }
            }

        } catch (Exception e) {
            // like there is no EXIF support?
            Log.e("ImageUtils", e);
        } catch (Throwable e) {
            // Out of stupid vedroid's memory
            Log.e("ImageUtils", e.toString());
        }

        return bitmap;
    }

    /**
     * Returns Bitmap rotated according to EXIF limiting the resulting image size.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid.
     *
     * @param targetFilePath
     * @param maxSideSize    - the biggest image dimension (height or width) limit
     * @return Bitmap or null if targetFile is null/not readable or OOM/other Exception happens
     */
    public static Bitmap getRotatedBitmapByExif(final String targetFilePath, final int maxSideSize) {
        if (targetFilePath == null || targetFilePath.length() == 0) {
            Log.e("File is not readable! " + targetFilePath);
            return null;
        }
        return getRotatedBitmapByExif(new File(targetFilePath), maxSideSize);
    }

    /**
     * Returns Bitmap rotated according to EXIF limiting the resulting image size.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid.
     *
     * @param targetFile
     * @param maxSideSize - the biggest image dimension (height or width) limit
     * @return Bitmap or null if targetFile is null/not readable or OOM/other Exception happens
     */
    public static Bitmap getRotatedBitmapByExif(final File targetFile, final int maxSideSize) {

        if (!FileUtils.isReadable(targetFile)) {
            Log.e("File is not readable! " + targetFile);
            return null;
        }

        Bitmap bitmap = null;
        // detecting if an image needs to be rotated
        try {
            final int rotateAngle = getExifRotateAngle(targetFile);
            boolean isRotationNeeded = rotateAngle > 0;
            if (isRotationNeeded) {
                bitmap = getRotatedBitmapByAngle(getBitmapFromFileWithMaxSideSize(targetFile, maxSideSize), rotateAngle);
            } else {
                bitmap = getBitmapFromFileWithMaxSideSize(targetFile, maxSideSize);
            }
        } catch (Exception e) {
            // like there is no EXIF support?
            Log.e("ImageUtils", e);
        } catch (Throwable e) {
            // Out of stupid vedroid's memory
            Log.e("ImageUtils", e.toString());
        }

        return bitmap;
    }

    /**
     * Returns Bitmap rotated according to EXIF limiting the resulting image size.
     * WARNING! Samsung and LG cameras has well known bug and their EXIF is invalid. This method
     * includes workaround so it may work for such devices.
     *
     * @param context
     * @param targetUri
     * @param maxSideSize - the biggest image dimension (height or width) limit
     * @return Bitmap or null if targetFile is null/not readable or OOM/other Exception happens
     */
    public static Bitmap getRotatedBitmapByExif(final Context context, final Uri targetUri, final int maxSideSize) {

        Bitmap bitmap = null;
        // detecting if an image needs to be rotated
        try {
            final int rotateAngle = (int) getExifRotateAngle(context, targetUri);
            boolean isRotationNeeded = rotateAngle > 0;
            final File targetFile = FileUtils.getFileFromUri(context, targetUri);
            if (!FileUtils.isReadable(targetFile)) {
                Log.e("File is not readable! " + targetFile);
                return null;
            }
            if (isRotationNeeded) {
                bitmap = getRotatedBitmapByAngle(getBitmapFromFileWithMaxSideSize(targetFile, maxSideSize), rotateAngle);
            } else {
                bitmap = getBitmapFromFileWithMaxSideSize(targetFile, maxSideSize);
            }
        } catch (Exception e) {
            // like there is no EXIF support?
            Log.e("ImageUtils", e);
        } catch (Throwable e) {
            // Out of stupid vedroid's memory
            Log.e("ImageUtils", e.toString());
        }

        return bitmap;
    }


    //*********************************************************************************************
    //
    // Image manipulating methods
    //
    //*********************************************************************************************

    /**
     * Returns square scaled down (stretched) Bitmap from source bitmap using parameter maxSideSize
     *
     * @param bitmap
     * @param maxSideSize
     * @return
     */
    public static Bitmap getSquareBitmap(final Bitmap bitmap, final int maxSideSize) {
        return getSquareBitmap(bitmap, maxSideSize, true);
    }

    /**
     * Returns square scaled down (stretched) Bitmap from source bitmap using parameter maxSideSize
     * Respects bitmap's aspect ratio if doKeepAspectRatioWhileStretch is true  and returns Bitmap
     * with alpha channel  (Config.ARGB_8888) for empty space.
     *
     * @param bitmap
     * @param sideSize
     * @param doKeepAspectRatioWhileStretch
     * @return
     */
    public static Bitmap getSquareBitmap(final Bitmap bitmap, final int sideSize, final boolean doKeepAspectRatioWhileStretch) {

        if (bitmap == null || sideSize == 0)
            return null;

        final BitmapInfo bitmapInfo = new BitmapInfo(bitmap);
        final int width = bitmapInfo.width;
        final int height = bitmapInfo.height;

        if (width <= sideSize && height <= sideSize)
            return bitmap;

        if (doKeepAspectRatioWhileStretch) {
            final float aspectRatio = (float) sideSize / (float) ((height > width) ? height : width);
            final int scaleHeight = (int) (height * aspectRatio);
            final int scaleWidth = (int) (width * aspectRatio);
            final int deltaW = (sideSize - scaleWidth) / 2;
            final int deltaH = (sideSize - scaleHeight) / 2;
            // create square bitmap with given maxSideSize dimensions
            final Bitmap resizedBitmap = Bitmap.createBitmap(sideSize, sideSize, Config.ARGB_8888);
            resizedBitmap.eraseColor(Color.TRANSPARENT);
            final RectF outRect = new RectF(deltaW, deltaH, scaleWidth + deltaW, scaleHeight + deltaH);
            final Canvas canvas = new Canvas(resizedBitmap);
            canvas.drawBitmap(bitmap, null, outRect, null);
            return resizedBitmap;
        } else {
            //makes square image/ Center crop!!!
            Matrix matrix = new Matrix();
            float scale = bitmapInfo.hasLandscapeOrientation ? ((float) sideSize) / height : ((float) sideSize) / width;
            matrix.postScale(scale, scale);
            if (bitmapInfo.hasLandscapeOrientation)
                return Bitmap.createBitmap(bitmap, width / 2 - height / 2, 0, height, height, matrix, true);
            else
                return Bitmap.createBitmap(bitmap, 0, height / 2 - width / 2, width, width, matrix, true);
        }
    }


    /**
     * Returns given Bitmap resized the to given dimensions using Matrix ignoring aspect ratio if
     * both dimensions set to value greater than 0. Respects image aspect ratio if one of
     * dimensions set to 0 but other set to particular value.
     * Returns null if given Bitmap is null or one dimensions is lower than 0 or both dimensions are
     * set to 0 or Exception (like OOM) happens.
     *
     * @param bitmap    Bitmap to resize
     * @param newHeight height to resize to
     * @param newWidth  width to resize to
     * @return resized Bitmap or null
     */
    public static Bitmap getResizedBitmap(final Bitmap bitmap, int newHeight, int newWidth) {
        if (bitmap == null || newHeight < 0 || newWidth < 0 || newHeight + newWidth == 0)
            return null;
        final BitmapInfo bitmapInfo = new BitmapInfo(bitmap);
        if (newHeight == 0)
            newHeight = (int) bitmapInfo.getHeightByWidth(newWidth);
        if (newWidth == 0)
            newWidth = (int) bitmapInfo.getWidthByHeight(newHeight);

        float scaleHeight = ((float) newHeight) / bitmapInfo.height;
        float scaleWidth = ((float) newWidth) / bitmapInfo.width;
        Log.d("ImageUtils", "width: " + bitmapInfo.width + " height: " + bitmapInfo.height + " newWidth: " + newWidth + " newHeight: " + newHeight + " scaleWidth: " + scaleWidth + " scaleHeight: " + scaleHeight);

        // create a matrix for the manipulation
        final Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap;
        try {
            // recreate the Bitmap
            resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapInfo.width, bitmapInfo.height, matrix, true);
        } catch (Exception e) {
            Log.e(e);
            resizedBitmap = null;
        } catch (Throwable e) {
            Log.e(e);
            resizedBitmap = null;
        }

        return resizedBitmap;
    }

    /**
     * Returns Bitmap being set to given @ImageView
     *
     * @param imageView
     * @return
     */
    public static Bitmap getBitmapFromImageView(final ImageView imageView) {
        if (imageView == null)
            return null;
        return getBitmapFromDrawable(imageView.getDrawable());
    }

    /**
     * This method returns a Bitmap related to given Drawable.
     *
     * @param drawable Drawable resource of image
     * @return @Bitmap whose resource id was passed to method.
     */
    public static Bitmap getBitmapFromDrawable(final Drawable drawable) {
        if (drawable == null)
            return null;

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Combines two images (overlay), as a result first image will be covered by the second one
     * resulting width is the biggest width of two images and same with height
     *
     * @param context      - to use to get drawable from resources (bitmap2ResId)
     * @param bitmap1      - bitmap to add to
     * @param bitmap2ResId - resource ID of drawable to add to bitmap1
     * @return
     */
    public static Bitmap getBitmapByAddImages(final Context context, final Bitmap bitmap1, final int bitmap2ResId) {
        if (context == null || bitmap1 == null)
            return null;
        final Bitmap bitmap2 = getBitmapFromResources(context, bitmap2ResId);
        final Bitmap result = getBitmapByAddImages(bitmap1, bitmap2);
        return result;
    }

    /**
     * Combines two images (overlay), as a result first image will be covered by the second one
     * resulting width is the biggest width of two images and same with height
     *
     * @param context      - to use to get drawable from resources (bitmap1ResId)
     * @param bitmap1ResId - resource ID of drawable to add bitmap2 to
     * @param bitmap2      - bitmap to be added
     * @return
     */
    public static Bitmap getBitmapByAddImages(final Context context, final int bitmap1ResId, final Bitmap bitmap2) {
        if (context == null || bitmap2 == null)
            return null;
        final Bitmap mBitmap1 = getBitmapFromResources(context, bitmap1ResId);
        final Bitmap result = getBitmapByAddImages(mBitmap1, bitmap2);
        return result;
    }

    /**
     * Combines two images (overlay), as a result first image will be covered by the second one
     * resulting width is the biggest width of two images and same with height
     *
     * @param bitmap1
     * @param bitmap2
     * @return
     */
    public static Bitmap getBitmapByAddImages(Bitmap bitmap1, final Bitmap bitmap2) {
        if (bitmap1 == null && bitmap2 == null)
            return null;
        if (bitmap2 == null)
            return bitmap1;
        if (bitmap1 == null)
            return bitmap2;

        int resultingWidth, resultingHeight;
        if (bitmap1.getWidth() > bitmap2.getWidth()) {
            resultingWidth = bitmap2.getWidth();
        } else {
            resultingWidth = bitmap1.getWidth();
        }
        if (bitmap1.getHeight() > bitmap2.getHeight()) {
            resultingHeight = bitmap1.getHeight();
        } else {
            resultingHeight = bitmap2.getHeight();
        }
        final Bitmap bitmap = Bitmap.createBitmap(resultingWidth, resultingHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap1, 0f, 0f, null);
        canvas.drawBitmap(bitmap2, 0f, 0f, null);
        return bitmap;
    }


    /**
     * Joins two images horizontally, 2nd image will be joined to the right of the 1st image.
     * Resulting height will be taken from the biggest (by height) image
     *
     * @param bitmapLeft  - bitmap to add to (stays at left in a resulting image)
     * @param bitmapRight - bitmap to be added to left bitmap (stays at right in a resulting image)
     * @return
     */
    public static Bitmap getBitmapOfJoinedImagesHorizontally(final Bitmap bitmapLeft, final Bitmap bitmapRight) {
        if (bitmapLeft == null && bitmapRight == null)
            return null;
        if (bitmapRight == null)
            return bitmapLeft;
        if (bitmapLeft == null)
            return bitmapRight;
        int resultingWidth, resultingHeight;
        resultingWidth = bitmapLeft.getWidth() + bitmapRight.getWidth();
        if (bitmapLeft.getHeight() > bitmapRight.getHeight()) {
            resultingHeight = bitmapLeft.getHeight();
        } else {
            resultingHeight = bitmapRight.getHeight();
        }
        final Bitmap bitmap = Bitmap.createBitmap(resultingWidth, resultingHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmapLeft, 0f, 0f, null);
        canvas.drawBitmap(bitmapRight, bitmapLeft.getWidth(), 0f, null);
        return bitmap;
    }

    /**
     * Joins two images vertically, the second image top will be joined to the 1st image bottom
     * Resulting width will be taken from the biggest (by width) image
     *
     * @param bitmapTop
     * @param bitmapBottom
     * @return
     */
    public static Bitmap getBitmapOfJoinedImagesVertically(final Bitmap bitmapTop, final Bitmap bitmapBottom) {
        if (bitmapTop == null && bitmapBottom == null)
            return null;
        if (bitmapBottom == null)
            return bitmapTop;
        if (bitmapTop == null)
            return bitmapBottom;
        int resultingWidth, resultingHeight;
        resultingHeight = bitmapTop.getHeight() + bitmapBottom.getHeight();
        if (bitmapTop.getWidth() > bitmapBottom.getWidth()) {
            resultingWidth = bitmapTop.getWidth();
        } else {
            resultingWidth = bitmapBottom.getWidth();
        }
        final Bitmap bitmap = Bitmap.createBitmap(resultingWidth, resultingHeight, Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmapTop, 0f, 0f, null);
        canvas.drawBitmap(bitmapBottom, 0f, bitmapTop.getHeight(), null);
        return bitmap;
    }

    /**
     * Returns Bitmap compressed to Jpeg with quality of targetJpegQuality
     * its is might be useful to make a preview of resulting quality loss
     *
     * @param bitmap
     * @param targetJpegQuality
     * @return
     */
    public static Bitmap getJpegFromBitmap(Bitmap bitmap, int targetJpegQuality) {
        if (bitmap == null || targetJpegQuality == 0)
            return bitmap;
        if (targetJpegQuality > 100 || targetJpegQuality < 0)
            targetJpegQuality = 100;

        // Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, targetJpegQuality, bos);
        bitmap.recycle();
        byte[] bitmapData = bos.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

        return bitmap;
    }


    /**
     * Saves given image to a File in a JPEG format with given quality
     *
     * @param bitmap
     * @param imageFile - target file to save JPEG to
     * @param quality   - percentage of quality, 1-100, 0 means 100
     * @return
     */
    public static boolean saveBitmapToJPEGFile(final Bitmap bitmap, final File imageFile, int quality) {
        if (bitmap == null || imageFile == null || imageFile.exists() && !imageFile.canWrite())
            return false;
        if (imageFile.exists() && !imageFile.delete())
            return false;
        if (quality > 100 || quality < 0)
            quality = 100;
        boolean isSucceed;
        // save it
        // Convert bitmap to byte array
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (quality == 0)
            bitmap.compress(CompressFormat.JPEG, 100, bos);
        else
            bitmap.compress(CompressFormat.JPEG, quality, bos);
        // write the bytes to file
        isSucceed = FileUtils.byteArrayOutputStreamToFile(bos, imageFile);
        if (bos != null)
            try {
                bos.close();
            } catch (IOException e) {
            }
        return isSucceed;
    }

    /**
     * Saves given image to a File in a PNG format
     *
     * @param bitmap
     * @param imageFile - target file to save PNG to
     * @return
     */
    public static boolean saveBitmapToPNGFile(final Bitmap bitmap, final File imageFile) {
        if (bitmap == null || imageFile == null || imageFile.exists() && !imageFile.canWrite())
            return false;
        if (imageFile.exists() && !imageFile.delete())
            return false;
        boolean isSucceed;
        // save it
        // Convert bitmap to byte array
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, bos);
        // write the bytes in file
        isSucceed = FileUtils.byteArrayOutputStreamToFile(bos, imageFile);
        if (bos != null)
            try {
                bos.close();
            } catch (IOException e) {
            }
        return isSucceed;
    }


    /**
     * Deletes a copy of a photo which could be created on some devices while using camera
     *
     * @param context
     */
    public static void deleteLastTakenPhotoDupe(final Context context) {
        if (context == null)
            return;
        String[] projection = new String[]{
                BaseColumns._ID,
                MediaColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaColumns.MIME_TYPE};

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
            if (cursor != null) {
                cursor.moveToFirst();
                // you will find the last taken picture here and can delete that
                final ContentResolver cr = context.getContentResolver();
                cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + cursor.getString(0), null);

            }
        } catch (Exception e) {
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * Returns blurred blurred copy of given image
     *
     * @param resources
     * @param drawableResId
     * @param radius - must be greater than 1
     * @return
     */
    public static Bitmap getBlurredBitmap(final Resources resources, final int drawableResId, final int radius) {
        final Bitmap bitmapToBlur = BitmapFactory.decodeResource(resources, drawableResId);
        blurBitmap(bitmapToBlur, radius);
        return bitmapToBlur;
    }

    /**
     * Returns blurred copy of given image
     *
     * @param bitmapToBlur
     * @param radius - must be greater than 1
     * @return
     */
    public static Bitmap getBlurredBitmap(final Bitmap bitmapToBlur, final int radius) {
        final Bitmap bitmap = bitmapToBlur.copy(bitmapToBlur.getConfig(), true);
        blurBitmap(bitmap, radius);
        return bitmap;
    }

    /**
     * Blurs given image
     *
     * @param bitmapToBlur - Bitmap to apply blur to
     * @param radius - must be greater than 1
     */
    public static void blurBitmap(final Bitmap bitmapToBlur, final int radius) {
        if (radius < 1)
            return;

        int w = bitmapToBlur.getWidth();
        int h = bitmapToBlur.getHeight();

        int[] pix = new int[w * h];
        // Log.e("pix", w + " " + h + " " + pix.length);
        bitmapToBlur.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmapToBlur.setPixels(pix, 0, w, 0, 0, w, h);
    }


    /**
     * Takes screenshot of root view of the activity.
     *
     * @param activity - target Activity
     * @return Bitmap or null if root View could not be found
     */
    public static Bitmap getScreenShotOfActivity(final Activity activity) {
        final View view = activity.findViewById(android.R.id.content);
        return getScreenShotOfView(view);
    }

    /**
     * Takes screenshot of view (identified by given id) of the activity.
     *
     * @param activity - Activity holding/managing target View
     * @param id - target View's ID
     * @return Bitmap or null if View could not be found
     */
    public static Bitmap getScreenShotOfActivityView(final Activity activity, final int id) {
        return getScreenShotOfView(activity.findViewById(id));
    }

    /**
     * Takes a screenshot of the view.
     *
     * @param view - target View
     * @return - Bitmap or null if View is null
     */
    public static Bitmap getScreenShotOfView(final View view) {
        if (view == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Takes a screenshot with deisred dimensions of the view.
     *
     * @param view - target View
     * @param width - desired width of screenshot
     * @param height - desired height of screenshot
     * @return - Bitmap or null if View is null
     */
    public static Bitmap getScreenShotOfView(final View view, final int width, final int height) {
        if (view == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Returns cropped from center image with previous scaling. Works with File.
     *
     * @param bitmapFile - File to get image from
     * @param cropToHeight - target height
     * @param cropToWidth - target width
     * @return - cropped Bitmap
     */
    public static Bitmap getBitmapByCropFromCenterWithScaling(final File bitmapFile, final int cropToHeight, final int cropToWidth) {
        if (bitmapFile == null || !FileUtils.isReadable(bitmapFile) || cropToHeight == 0 || cropToWidth == 0)
            return null;

        final BitmapInfo bitmapInfo = ImageUtils.getBitmapInfoFromFile(bitmapFile);
        if (bitmapInfo == null)
            return null;

        int cropHeight = cropToHeight, cropWidth = cropToWidth;
        float bitmapWHProportions = bitmapInfo.width / bitmapInfo.height;
        float bitmapHWProportions = bitmapInfo.height / bitmapInfo.width;
        float cropWHProportions = (float) cropWidth / (float) cropHeight;

        Bitmap bitmapToCrop;
        // crop Portrait from Portrait
        if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropHeight > cropWidth) {
            // 480x800 -> 300x400, result is wider than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                if (cropWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropWidth;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
            } else {
                // scale by height
                if (cropHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropHeight;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropWidth > cropHeight) {
            // 800x480 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropWidth;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
            } else {
                // scale by height
                if (cropHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropHeight;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Portrait
        else if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropHeight < cropWidth) {
            // 480x800 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            // scale by width
            if (cropWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropWidth;
                cropWidth *= downSample;
                cropHeight *= downSample;
            } else if (cropHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropHeight;
                cropWidth *= downSample;
                cropHeight *= downSample;
            }
            Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropWidth);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
        }
        // crop Portrait from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropWidth < cropHeight /*&& bitmapInfo.height>cropToHeight*/) {
            // scale by height
            if (cropHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropHeight;
                cropWidth *= downSample;
                cropHeight *= downSample;
            } else if (cropWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropWidth;
                cropWidth *= downSample;
                cropHeight *= downSample;
            }
            Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropHeight);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
        } else {
            if (bitmapInfo.hasLandscapeOrientation)
                bitmapToCrop = getBitmapFromFileWithMaxWidth(bitmapFile, (int) bitmapInfo.width);
            else
                bitmapToCrop = getBitmapFromFileWithMaxHeight(bitmapFile, (int) bitmapInfo.height);
        }

        Log.d("original bitmap H: " + bitmapInfo.height + " W: " + bitmapInfo.width);
        return getCroppedFromCenterBitmap(bitmapToCrop, cropHeight, cropWidth);
    }

    /**
     * Returns cropped from center image with previous scaling. Works with Resources.
     * Uses Context from Initializer so it must be initialized before
     *
     * @param bitmapResId - resource ID to get image of
     * @param cropToHeight - target height
     * @param cropToWidth - target width
     * @return - cropped Bitmap
     */
    public static Bitmap getBitmapByCropFromCenterWithScaling(final int bitmapResId, final int cropToHeight, final int cropToWidth) {
        return getBitmapByCropFromCenterWithScaling(Initializer.getsAppContext(), bitmapResId, cropToHeight, cropToWidth);
    }

    /**
     * Returns cropped from center image with previous scaling. Works with Resources.
     *
     * @param context - to have access to resources
     * @param bitmapResId - resource ID to get image of
     * @param cropToHeight - target height
     * @param cropToWidth - target width
     * @return - cropped Bitmap or null if given context, bitmap is null or of dimensions is 0
     */
    public static Bitmap getBitmapByCropFromCenterWithScaling(final Context context, final int bitmapResId, final int cropToHeight, final int cropToWidth) {
        if (context == null || bitmapResId == 0 || cropToHeight == 0 || cropToWidth == 0)
            return null;

        final BitmapInfo bitmapInfo = ImageUtils.getBitmapInfoFromResources(context, bitmapResId);
        if (bitmapInfo == null) {
            return null;
        }

        int cropHeight = cropToHeight, cropWidth = cropToWidth;
        float bitmapWHProportions = bitmapInfo.width / bitmapInfo.height;
        float bitmapHWProportions = bitmapInfo.height / bitmapInfo.width;
        float cropWHProportions = (float) cropWidth / (float) cropHeight;

        Bitmap bitmapToCrop;
        // crop Portrait from Portrait
        if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropHeight > cropWidth) {
            // 480x800 -> 300x400, result is wider than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropWidth;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxWidth(context, bitmapResId, cropWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
            } else {
                // scale by height
                if (cropHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropHeight;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxHeight(context, bitmapResId, cropHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropWidth > cropHeight) {
            // 800x480 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropWidth;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxWidth(context, bitmapResId, cropWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
            } else {
                // scale by height
                if (cropHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropHeight;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxHeight(context, bitmapResId, cropHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Portrait
        else if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropHeight < cropWidth && bitmapInfo.width > cropWidth) {
            // 480x800 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            // scale by width
            if (cropWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropWidth;
                cropWidth *= downSample;
                cropHeight *= downSample;
            } else if (cropHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropHeight;
                cropWidth *= downSample;
                cropHeight *= downSample;
            }
            final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxWidth(context, bitmapResId, cropWidth);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
        }
        // crop Portrait from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropWidth < cropHeight /*&& bitmapInfo.height>cropToHeight*/) {
            // scale by height
            if (cropHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropHeight;
                cropWidth *= downSample;
                cropHeight *= downSample;
            } else if (cropWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropWidth;
                cropWidth *= downSample;
                cropHeight *= downSample;
            }
            final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxHeight(context, bitmapResId, cropHeight);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
        } else {
            if (bitmapInfo.hasLandscapeOrientation)
                bitmapToCrop = getBitmapFromResourcesWithMaxWidth(context, bitmapResId, (int) bitmapInfo.width);
            else
                bitmapToCrop = getBitmapFromResourcesWithMaxHeight(context, bitmapResId, (int) bitmapInfo.height);
        }

        return getCroppedFromCenterBitmap(bitmapToCrop, cropHeight, cropWidth);
    }

    /**
     * Returns cropped from center image.
     *
     * @param bitmapToScale - image to crop&scale
     * @param cropToHeight - target height
     * @param cropToWidth - target width
     * @return - cropped Bitmap or null if given bitmap is null or of dimensions is 0
     */
    public static Bitmap getBitmapByCropFromCenterWithScaling(final Bitmap bitmapToScale, final int cropToHeight, final int cropToWidth) {
        if (bitmapToScale == null || cropToHeight == 0 || cropToWidth == 0)
            return null;

        final BitmapInfo bitmapInfo = new BitmapInfo(bitmapToScale.getWidth(), bitmapToScale.getHeight());
        int cropHeight = cropToHeight, cropWidth = cropToWidth;
        float bitmapWHProportions = bitmapInfo.width / bitmapInfo.height;
        float bitmapHWProportions = bitmapInfo.height / bitmapInfo.width;
        float cropWHProportions = (float) cropWidth / (float) cropHeight;

        Bitmap bitmapToCrop;
        // crop Portrait from Portrait
        if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropHeight > cropWidth) {
            // 480x800 -> 300x400, result is wider than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropWidth;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }

                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
            } else {
                // scale by height
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
                if (cropHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropHeight;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropWidth > cropHeight) {
            // 800x480 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropWidth;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
            } else {
                // scale by height
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
                if (cropHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropHeight;
                    cropWidth *= downSample;
                    cropHeight *= downSample;
                }
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Portrait
        else if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropHeight < cropWidth /*&& bitmapInfo.width>cropToWidth*/) {
            // 480x800 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            // scale by width
            //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
            if (cropWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropWidth;
                cropWidth *= downSample;
                cropHeight *= downSample;
            } else if (cropHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropHeight;
                cropWidth *= downSample;
                cropHeight *= downSample;
            }
            bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropWidth * bitmapHWProportions), cropWidth);
        }
        // crop Portrait from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropWidth < cropHeight /*&& bitmapInfo.height>cropToHeight*/) {
            // scale by height
            //Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
            if (cropHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropHeight;
                cropWidth *= downSample;
                cropHeight *= downSample;
            } else if (cropWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropWidth;
                cropWidth *= downSample;
                cropHeight *= downSample;
            }
            bitmapToCrop = getResizedBitmap(bitmapToScale, cropHeight, (int) (cropHeight * bitmapWHProportions));
        } else {
            bitmapToCrop = bitmapToScale;
        }

        return getCroppedFromCenterBitmap(bitmapToCrop, cropHeight, cropWidth);
    }

    /**
     * Returns Bitmap cropped from center of given image
     *
     * @param bitmapToCrop - Bitmap to crop from
     * @param cropToHeight - target height
     * @param cropToWidth - target width
     * @return - cropped Bitmap or null if given bitmap is null or of dimensions is 0
     */
    public static Bitmap getCroppedFromCenterBitmap(final Bitmap bitmapToCrop, final int cropToHeight, final int cropToWidth) {
        if (bitmapToCrop == null || cropToHeight == 0 || cropToWidth == 0)
            return null;

        if (bitmapToCrop.getWidth() > cropToWidth && bitmapToCrop.getHeight() > cropToHeight) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, deltaY,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getWidth() > cropToWidth && bitmapToCrop.getHeight() == cropToHeight) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, 0,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getWidth() == cropToWidth && bitmapToCrop.getHeight() > cropToHeight) {
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    0, deltaY,
                    cropToWidth, cropToHeight);
        } else
            return bitmapToCrop;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    /**
     * Sets given Bitmap image to background of given View according to API level
     *
     * @param view
     * @param bitmap
     */
    public static void setBackground(final View view, final Bitmap bitmap) {
        if (view == null || bitmap == null)
            return;
        if (DeviceInfo.hasAPI(16))
            view.setBackground(ImageUtils.getDrawableFromBitmap(bitmap, view.getContext()));
        else
            view.setBackgroundDrawable(ImageUtils.getDrawableFromBitmap(bitmap, view.getContext()));
    }

    /**
     * Sets given drawable image (drawableResId) to background of given View according to API level
     *
     * @param view
     * @param drawableResId
     */
    public static void setBackground(View view, int drawableResId) {
        if (view == null || drawableResId == 0)
            return;
        view.setBackgroundResource(drawableResId);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    /**
     * Sets given Drawable image to background of given View according to API level
     *
     * @param view
     * @param bitmap
     */
    public static void setBackground(final View view, final BitmapDrawable bitmap) {
        if (view == null || bitmap == null)
            return;
        if (DeviceInfo.hasAPI(16))
            view.setBackground(bitmap);
        else
            view.setBackgroundDrawable(bitmap);
    }

    /**
     * Returns byte array of JPEG compressed Bitmap image
     *
     * @param bitmap - bitmap to compress to JPEG
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromBitmap(final Bitmap bitmap, final int jpegQuality) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, jpegQuality, baos);
        return baos.toByteArray();
    }

    /**
     * Returns byte array of JPEG compressed image from File
     *
     * @param imageFilePath - image file path
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromFile(final String imageFilePath, final int jpegQuality) {
        return getJPEGByteArrayFromFile(new File(imageFilePath), jpegQuality);
    }

    /**
     * Returns byte array of JPEG compressed image from File
     *
     * @param imageFile - image File
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromFile(final File imageFile, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getBitmapFromFile(imageFile);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of JPEG compressed image from given File
     *
     * @param imageFile - image File
     * @param maxSideSize - limit of the bigger dimension
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromFile(final String imageFile, final int maxSideSize, final int jpegQuality) {
        return getJPEGByteArrayFromFile(new File(imageFile), maxSideSize, jpegQuality);
    }

    /**
     * Returns byte array of JPEG compressed image from given File
     *
     * @param imageFile - image File
     * @param maxSideSize - limit of the bigger dimension
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromFile(final File imageFile, final int maxSideSize, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getBitmapFromFileWithMaxSideSize(imageFile, maxSideSize);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of JPEG compressed image from given drawable resource
     *
     * @param imageResId - image drawable resource ID (R.drawable.ID)
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromResources(final int imageResId, final int jpegQuality) {
        return getJPEGByteArrayFromResources(Initializer.getsAppContext(), imageResId, jpegQuality);
    }

    /**
     * Returns byte array of JPEG compressed image from given drawable resource
     *
     * @param context - to get access to Resources
     * @param imageResId - drawable resource ID (R.drawable.ID)
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromResources(final Context context, final int imageResId, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getBitmapFromResources(context, imageResId);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of JPEG compressed image from given drawable resource
     *
     * @param imageResId - drawable resource ID (R.drawable.ID)
     * @param maxSideSize - limit of bigger image side size
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromResources(final int imageResId, final int maxSideSize, final int jpegQuality) {
        return getJPEGByteArrayFromResources(Initializer.getsAppContext(), imageResId, maxSideSize, jpegQuality);
    }

    /**
     * Returns byte array of JPEG compressed image from given drawable resource
     *
     * @param context - to get access to Resources
     * @param imageResId - drawable resource ID (R.drawable.ID)
     * @param maxSideSize - limit of bigger image side size
     * @param jpegQuality - target JPEG quality
     * @return
     */
    public static byte[] getJPEGByteArrayFromResources(final Context context, final int imageResId, final int maxSideSize, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getResizedBitmapFromResources(context, imageResId, maxSideSize, maxSideSize);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param bitmap - image to compress
     * @return
     */
    public static byte[] getPNGByteArrayFromBitmap(final Bitmap bitmap) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, baos);
        return baos.toByteArray();
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param imageFile - image File path
     * @return
     */
    public static byte[] getPNGByteArrayFromFile(final String imageFile) {
        return getPNGByteArrayFromFile(new File(imageFile));
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param image - image File
     * @return
     */
    public static byte[] getPNGByteArrayFromFile(final File image) {
        final Bitmap bm = ImageUtils.getBitmapFromFile(image);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param imageFile - image File path
     * @param maxSideSize - limit of bigger image side size
     * @return
     */
    public static byte[] getPNGByteArrayFromFile(final String imageFile, final int maxSideSize) {
        return getPNGByteArrayFromFile(new File(imageFile), maxSideSize);
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param image - image File
     * @param maxSideSize - limit of bigger image side size
     * @return
     */
    public static byte[] getPNGByteArrayFromFile(final File image, final int maxSideSize) {
        final Bitmap bm = ImageUtils.getBitmapFromFileWithMaxSideSize(image, maxSideSize);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param context - to get access to Resources
     * @param imageResId - drawable resource ID (R.drawable.ID)
     * @return
     */
    public static byte[] getPNGByteArrayFromResources(final Context context, final int imageResId) {
        final Bitmap bm = ImageUtils.getBitmapFromResources(context, imageResId);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    /**
     * Returns byte array of PNG compressed image
     *
     * @param context - to get access to Resources
     * @param imageResId - drawable resource ID (R.drawable.ID)
     * @param maxSideSize - limit of bigger image side size
     * @return
     */
    public static byte[] getPNGByteArrayFromResources(final Context context, final int imageResId, final int maxSideSize) {
        final Bitmap bm = ImageUtils.getResizedBitmapFromResources(context, imageResId, maxSideSize, maxSideSize);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    /**
     * Returns round (circle) image
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundBitmap(final Bitmap bitmap) {
        return getCircleWrappedBitmap(bitmap);
    }

    /**
     * Returns round (circle) image
     *
     * @param bitmap
     * @param diameter
     * @return
     */
    public static Bitmap getRoundBitmap(final Bitmap bitmap, final int diameter) {
        return getCircleWrappedBitmap(bitmap, diameter);
    }

    /**
     * Returns round (circle) image
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getCircleBitmap(final Bitmap bitmap) {
        return getCircleWrappedBitmap(bitmap);
    }

    /**
     * Returns round (circle) image of given diameter
     *
     * @param bitmap
     * @param diameter
     * @return
     */
    public static Bitmap getCircleBitmap(final Bitmap bitmap, final int diameter) {
        return getCircleWrappedBitmap(bitmap, diameter);
    }

    /**
     * Returns round (circle) image with diameter of smallest image side size
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getCircleWrappedBitmap(final Bitmap bitmap) {
        if (bitmap == null)
            return null;
        return getCircleWrappedBitmap(bitmap, Math.min(bitmap.getWidth(), bitmap.getHeight()));
    }

    /**
     * Returns round (circle) image of given diameter
     *
     * @param bitmap
     * @param diameter
     * @return
     */
    public static Bitmap getCircleWrappedBitmap(final Bitmap bitmap, int diameter) {
        if (bitmap == null)
            return null;
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        if (diameter == 0)
            diameter = cropSize;
        final int radius = diameter / 2;
        final Bitmap croppedToSquareBitmap = getBitmapByCropFromCenterWithScaling(bitmap, cropSize, cropSize);
        if (croppedToSquareBitmap != bitmap)
            bitmap.recycle();
        final Bitmap downsizedToDiameterBitmap;
        if (diameter == cropSize)
            downsizedToDiameterBitmap = croppedToSquareBitmap;
        else {
            downsizedToDiameterBitmap = getResizedBitmap(croppedToSquareBitmap, diameter, diameter);
            if (downsizedToDiameterBitmap != croppedToSquareBitmap)
                croppedToSquareBitmap.recycle();
        }
        final Bitmap outputBitmap = Bitmap.createBitmap(diameter, diameter, Config.ARGB_8888);
        final Path path = new Path();
        path.addCircle(radius, radius, radius, Path.Direction.CCW);
        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(downsizedToDiameterBitmap, 0, 0, null);
        if (outputBitmap != downsizedToDiameterBitmap)
            downsizedToDiameterBitmap.recycle();
        return outputBitmap;
    }

    /**
     * Returns Black&White (grey scaled) image
     *
     * @param orginalBitmap
     * @return
     */
    public static Bitmap getBlackAndWhiteBitmap(final Bitmap orginalBitmap) {
        return getBlackAndWhiteBitmap(orginalBitmap, Bitmap.Config.ARGB_8888);
    }

    /**
     * Returns Black&White (grey scaled) image
     *
     * @param orginalBitmap
     * @return
     */
    public static Bitmap getBlackAndWhiteBitmapARGB8888(final Bitmap orginalBitmap) {
        return getBlackAndWhiteBitmap(orginalBitmap, Bitmap.Config.ARGB_8888);
    }

    /**
     * Returns Black&White (grey scaled) image
     *
     * @param orginalBitmap
     * @return
     */
    public static Bitmap getBlackAndWhiteBitmapRGB565(final Bitmap orginalBitmap) {
        return getBlackAndWhiteBitmap(orginalBitmap, Bitmap.Config.RGB_565);
    }

    /**
     * Returns Black&White (grey scaled) image
     *
     * @param orginalBitmap
     * @param bitmapConfig
     * @return
     */
    public static Bitmap getBlackAndWhiteBitmap(final Bitmap orginalBitmap, Bitmap.Config bitmapConfig) {
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        final ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        final Bitmap blackAndWhiteBitmap = orginalBitmap.copy(bitmapConfig, true);
        final Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);
        final Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);
        return blackAndWhiteBitmap;
    }

    /**
     * Returns rotated by given angle bitmap
     *
     * @param bitmap
     * @param angle
     * @return
     */
    public static Bitmap getRotatedBitmapByAngle(final Bitmap bitmap, final float angle) {
        final Matrix matrix = new Matrix();
        matrix.postRotate(angle, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Returns Base64 encoded byte[] of image
     *
     * @param imageFile
     * @return
     */
    public String getBase64EncodedImage(final File imageFile) {
        return FileUtils.getBase64EncodedFile(imageFile);
    }

    /**
     * Returns Base64 encoded byte[] of image
     *
     * @param image
     * @return
     */
    public String getBase64EncodedPNG(final Bitmap image) {
        String imageDataString = "";
        try {
            byte imageData[] = getPNGByteArrayFromBitmap(image);
            // Converting Image byte array into Base64 String
            imageDataString = Base64.encodeToString(imageData, Base64.DEFAULT);
        } catch (OutOfMemoryError e) { //most probably RTE
            e.printStackTrace();
        }
        return imageDataString;
    }

    /**
     * Returns Base64 encoded byte[] of image
     *
     * @param image
     * @param jpegQuality
     * @return
     */
    public String getBase64EncodedJPEG(final Bitmap image, final int jpegQuality) {
        String imageDataString = null;
        try {
            byte imageData[] = getJPEGByteArrayFromBitmap(image, jpegQuality);
            // Converting Image byte array into Base64 String
            imageDataString = Base64.encodeToString(imageData, Base64.DEFAULT);
        } catch (OutOfMemoryError e) { //most probably RTE
            e.printStackTrace();
        }
        return imageDataString;
    }

    /**
     * Returns Drawable converted from Bitmap, uses Initializer context
     *
     * @param bitmap
     * @return
     */
    public static Drawable getDrawableFromBitmap(final Bitmap bitmap) {
        if (bitmap == null)
            return null;
        final Drawable drawable = new BitmapDrawable(Initializer.getResources(), bitmap);
        return drawable;
    }

    /**
     * Returns Drawable converted from Bitmap
     *
     * @param bitmap
     * @param context
     * @return
     */
    public static Drawable getDrawableFromBitmap(final Bitmap bitmap, final Context context) {
        if (bitmap == null || context == null)
            return null;
        final Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        return drawable;
    }


}
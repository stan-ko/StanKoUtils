package com.stanko.image;

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
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.stanko.tools.DeviceInfo;
import com.stanko.tools.FileUtils;
import com.stanko.tools.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * class used to store/restore of images
 *
 * @author Stan Koshutsky
 *         depends on class DeviceInfo
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    public static BitmapInfo getBitmapInfoFromFile(final File bitmapFile) {
        if (!FileUtils.isReadable(bitmapFile))
            return null;

        BitmapInfo bitmapInfo = null;
        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        // decode image size
        BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
        bmfOtions.inJustDecodeBounds = true;
        bmfOtions.inPurgeable = true;
        try {
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
            else
                BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);

            bitmapInfo = new BitmapInfo(bmfOtions.outWidth, bmfOtions.outHeight);
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

    public static BitmapInfo getBitmapInfoFromStream(final InputStream inputStream) {
        if (inputStream == null)
            return null;

        BitmapInfo bitmapInfo = null;

        final BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
        bmfOtions.inJustDecodeBounds = true;
        bmfOtions.inPurgeable = true;

        try {
            // decode image size
            BitmapFactory.decodeStream(inputStream, null, bmfOtions);
            bitmapInfo = new BitmapInfo(bmfOtions.outWidth, bmfOtions.outHeight);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return bitmapInfo;
    }

    public static BitmapInfo getBitmapInfoFromUri(final Uri uri, final Context context) {
        if (context == null || uri == null)
            return null;
        ContentResolver contentResolver = context.getContentResolver();
        return getBitmapInfoFromUri(uri, contentResolver);
    }

    public static BitmapInfo getBitmapInfoFromUri(final Uri uri, final ContentResolver contentResolver) {
        if (uri == null)
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

    public static BitmapInfo getBitmapInfoFromResources(final Context context, final int mBitmapResId) {
        if (context == null || mBitmapResId == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(context, mBitmapResId);
        return getBitmapInfoFromUri(uri, context);
    }

    public static BitmapInfo getBitmapInfoFromResources(final Resources resources, final ContentResolver contentResolver, final int mBitmapResId) {
        if (resources == null)
            return null;
        Uri uri = getUriOfBitmapFromResources(resources, mBitmapResId);
        return getBitmapInfoFromUri(uri, contentResolver);
    }

    public static BitmapInfo getBitmapInfoFromResId(final int mBitmapResId, final Context context) {
        if (context == null || mBitmapResId == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(context, mBitmapResId);
        return getBitmapInfoFromUri(uri, context);
    }

    public static class BitmapInfo {

        public final float height;
        public final float width;
        public final boolean hasPortraitOrientation;
        public final boolean hasLandscapeOrientation;
        public final boolean hasSquareForm;

        public BitmapInfo(final float width, final float height) {
            this.height = height;
            this.width = width;
            hasPortraitOrientation = height > width;
            hasLandscapeOrientation = width > height;
            hasSquareForm = width == height;
        }

        public BitmapInfo(final Bitmap bitmap) {
            this.height = bitmap.getHeight();
            this.width = bitmap.getWidth();
            hasPortraitOrientation = height > width;
            hasLandscapeOrientation = width > height;
            hasSquareForm = width == height;
        }

        public float getHeightByWidth(final float newWidth) {
            return (newWidth * height / width);
        }

        public float getWidthByHeight(final float newHeight) {
            return (newHeight * width / height);
        }

        @Override
        public String toString() {
            return "height: " + height + " width: " + width + " hasLandscapeOrientation: " + hasLandscapeOrientation + " hasPortraitOrientation: " + hasPortraitOrientation + " " + " hasSquareForm: " + hasSquareForm;
        }
    }


    //decodes image and scales it to reduce memory consumption
    public static Bitmap getBitmapFromFile(final String bitmapFile) {
        if (bitmapFile == null || bitmapFile.length() == 0)
            return null;
        return getBitmapFromFile(new File(bitmapFile), 0);
    }

    public static Bitmap getBitmapFromFile(final File bitmapFile) {
        return getBitmapFromFile(bitmapFile, 0);
    }

    public static Bitmap getBitmapFromFile(final String bitmapFile, final int sideSizeLimit) {
        return getBitmapFromFile(bitmapFile, sideSizeLimit, false);
    }

    public static Bitmap getBitmapFromFile(final String bitmapFile, final int sideSizeLimit, final boolean isOptimistic) {
        if (bitmapFile == null || bitmapFile.length() == 0)
            return null;
        return getBitmapFromFile(new File(bitmapFile), sideSizeLimit, isOptimistic);
    }

    public static Bitmap getBitmapFromFile(final File bitmapFile, final int sideSizeLimit) {
        return getBitmapFromFile(bitmapFile, sideSizeLimit, false);
    }

    public static Bitmap getBitmapFromFile(final File bitmapFile, final int sideSizeLimit, final boolean isOptimistic) {
        if (!FileUtils.isReadable(bitmapFile))
            return null;

        int maxWidth = 0, maxHeight = 0;
        if (sideSizeLimit > 0) {
            maxWidth = sideSizeLimit;
            maxHeight = sideSizeLimit;
        } else if (DeviceInfo.displayWidth > 0 && DeviceInfo.displayHeight > 0) {
            maxWidth = DeviceInfo.displayWidth;
            maxHeight = DeviceInfo.displayHeight;
        } else {
            maxWidth = DeviceInfo.getDeviceMaxSideSizeByDensity();
            maxHeight = maxWidth;
        }

        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        // decode image size
        BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
        bmfOtions.inJustDecodeBounds = true;
        bmfOtions.inPurgeable = true;
        try {
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
            else
                BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);

//			fileInputStream = new FileInputStream(bitmapFile);
//			BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ignored) {
                }

            // Find the correct scale value. It should be the power of 2.
            // final int REQUIRED_SIZE=70;
            int width_tmp = bmfOtions.outWidth, height_tmp = bmfOtions.outHeight;
            int scale = 1;
            while (width_tmp > maxWidth || height_tmp > maxHeight) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            if (isOptimistic && scale > 1)
                scale--;
            bmfOtions.inSampleSize = scale;
            bmfOtions.inJustDecodeBounds = false;

            // decode with inSampleSize
            // BitmapFactory.Options o2 = new BitmapFactory.Options();
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
            else
                return BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
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


    public static boolean rotateBitmapByExifAndSave(final String targetFilePath) {
        if (targetFilePath == null || targetFilePath.length() == 0)
            return false;
        return rotateBitmapByExifAndSave(new File(targetFilePath));
    }

    public static boolean rotateBitmapByExifAndSave(final File targetFile) {

        if (!FileUtils.isWritable(targetFile))
            return false;

        boolean isSucceed = false;
        // определяем необходимость поворота фотки
        try {
            final Matrix matrix = new Matrix();

            ExifInterface exifReader = new ExifInterface(targetFile.getAbsolutePath());

            int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            boolean isRotationNeeded = true;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;

                default: // ExifInterface.ORIENTATION_NORMAL
                    // Do nothing. The original image is fine.
                    isRotationNeeded = false;
                    isSucceed = true;
                    break;
            }

            if (isRotationNeeded) {
                BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
                bmfOtions.inPurgeable = true;
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
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
                    else
                        bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);

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


        } catch (IOException e) {
            isSucceed = false;
            Log.e(TAG, e);
        } catch (Exception e) {
            // like there is no EXIF support?
            isSucceed = false;
            Log.e(TAG, e);
        } catch (Throwable e) {
            // Out of stupid vedroid's memory
            isSucceed = false;
            Log.e(TAG, e.toString());
        }

        return isSucceed;
    }

    public static Bitmap getRotatedBitmapByExif(final String targetFilePath) {
        if (targetFilePath == null || targetFilePath.length() == 0)
            return null;
        return getRotatedBitmapByExif(new File(targetFilePath));
    }

    public static Bitmap getRotatedBitmapByExif(final File targetFile) {

        if (!FileUtils.isWritable(targetFile))
            return null;

        Bitmap bitmap = null;
        // определяем необходимость поворота фотки
        try {
            final Matrix matrix = new Matrix();

            ExifInterface exifReader = new ExifInterface(targetFile.getAbsolutePath());

            int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            boolean isRotationNeeded = false;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    isRotationNeeded = true;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    isRotationNeeded = true;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    isRotationNeeded = true;
                    break;

                default: // ExifInterface.ORIENTATION_NORMAL
                    // Do nothing. The original image is fine.
                    break;
            }

            BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
            bmfOtions.inPurgeable = true;

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
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
                    else
                        bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
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
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
                    else
                        bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
                } catch (FileNotFoundException e) {
                } finally {
                    if (fileInputStream != null)
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                }
            }

        } catch (IOException e) {
            Log.e("ImageUtils", e);
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
     * Returns scaled (stretched) @Bitmap from source bitmap using parameter maxSideSize
     *
     * @param bitmap
     * @param maxSideSize
     * @return
     */
    public static Bitmap getScaledBitmap(final Bitmap bitmap, final int maxSideSize) {
        return getScaledBitmap(bitmap, maxSideSize, true);
    }

    /**
     * Returns scaled (stretched) @Bitmap from source bitmap using parameter maxSideSize
     * respects bitmap's aspect ratio if doKeepAspectRatioWhileStrech==true
     *
     * @param bitmap
     * @param maxSideSize
     * @param doKeepAspectRatioWhileStrech
     * @return
     */
    public static Bitmap getScaledBitmap(final Bitmap bitmap, final int maxSideSize, final boolean doKeepAspectRatioWhileStrech) {

        // default image
        if (bitmap == null || maxSideSize == 0)
            return null;

        final int maxSideSize4Icon = maxSideSize;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        if (width <= maxSideSize4Icon && height <= maxSideSize4Icon)
            return bitmap;

        if (doKeepAspectRatioWhileStrech) {
            final float aspectRatio = (float) maxSideSize4Icon / (float) ((height > width) ? height : width);
            final int scaleHeight = (int) (height * aspectRatio);
            final int scaleWidth = (int) (width * aspectRatio);
            final Bitmap resizedIcon = Bitmap.createBitmap(maxSideSize4Icon, maxSideSize4Icon, Config.ARGB_8888);
            resizedIcon.eraseColor(Color.TRANSPARENT);
            final int deltaW = (maxSideSize4Icon - scaleWidth) / 2;
            final int deltaH = (maxSideSize4Icon - scaleHeight) / 2;

            final RectF outRect = new RectF(deltaW, deltaH, scaleWidth + deltaW, scaleHeight + deltaH);
            final Canvas canvas = new Canvas(resizedIcon);
            canvas.drawBitmap(bitmap, null, outRect, null);
            return resizedIcon;
        } else {
            //bm.recycle();
            //makes square image/ Center crop!!!
            final boolean isLandscapeImage = width >= height;
//			if (isLandscapeImage)
//				return  Bitmap.createBitmap( bm, width/2 - height/2, 0, height, height );
//			else
//				return  Bitmap.createBitmap( bm, 0, height/2 - width/2, width, width );

            Matrix matrix = new Matrix();
            float scale = isLandscapeImage ? ((float) maxSideSize) / height : ((float) maxSideSize) / width;
            matrix.postScale(scale, scale);
            if (isLandscapeImage)
                return Bitmap.createBitmap(bitmap, width / 2 - height / 2, 0, height, height, matrix, true);
            else
                return Bitmap.createBitmap(bitmap, 0, height / 2 - width / 2, width, width, matrix, true);
        }
    }


    /**
     * Returns @Bitmap compressed to Jpeg with quality of targetJpegQuality
     *
     * @param bitmap
     * @param targetJpegQuality
     * @return
     */
    public static Bitmap getReducedQualityJpegFromBitmap(Bitmap bitmap, int targetJpegQuality) {
        if (bitmap == null || targetJpegQuality == 0)
            return bitmap;
        if (targetJpegQuality > 100 || targetJpegQuality < 0)
            targetJpegQuality = 100;

        // Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, targetJpegQuality, bos);
        bitmap.recycle();
        byte[] bitmapdata = bos.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

        return bitmap;
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
        } else {
            return bitmap.getByteCount();
        }
    }

//	public Bitmap decodeImage(String imagePath)
//	 {  
//	     Bitmap bitmap=null;
//	     try
//	     {
//
//	         File file=new File(imagePath);
//	         BitmapFactory.Options o = new BitmapFactory.Options();
//	         o.inJustDecodeBounds = true;
//
//	         BitmapFactory.decodeStream(new FileInputStream(file),null,o);
//	         final int REQUIRED_SIZE=200;
//	         int width_tmp=o.outWidth, height_tmp=o.outHeight;
//
//	         int scale=1;
//	         while(true)
//	         {
//	             if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
//	             break;
//	             width_tmp/=2;
//	             height_tmp/=2;
//	             scale*=2;  
//	         }  
//
//	         BitmapFactory.Options options=new BitmapFactory.Options();
//
//	         options.inSampleSize=scale;
//	         bitmap=BitmapFactory.decodeStream(new FileInputStream(file), null, options);
//
//	     }  
//	     catch(Exception e) 
//	     {  
//	         bitmap = null;
//	     }      
//	     return bitmap; 
//	 }

    /**
     * Returns given @Bitmap resized the to given size.
     *
     * @param bitmap    : Bitmap to resize.
     * @param newHeight : Height to resize.
     * @param newWidth  : Width to resize.
     * @return Resized Bitmap.
     */
    public static Bitmap getResizedBitmap(final Bitmap bitmap, int newHeight, int newWidth) {

        if (bitmap == null || newHeight < 0 || newWidth < 0 || newHeight + newWidth == 0)
            return null;

        Bitmap resizedBitmap = null;
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();

        if (newHeight == 0)
            newHeight = (int) ((float) newWidth * (float) height / (float) width);

        if (newWidth == 0)
            newWidth = (int) ((float) newHeight * (float) width / (float) height);

        float scaleHeight = ((float) newHeight) / height;
        float scaleWidth = ((float) newWidth) / width;
        Log.d("ImageUtils", "width: " + width + " height: " + height + " newWidth: " + newWidth + " newHeight: " + newHeight + " scaleWidth: " + scaleWidth + " scaleHeight: " + scaleHeight);

        // create a matrix for the manipulation
        final Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        try {
            // recreate the new Bitmap
            resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            // resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, // newHeight, true);
        } catch (Exception e) {
            android.util.Log.e(TAG, e.getMessage());
            resizedBitmap = null;
        } catch (Throwable e) {
            android.util.Log.e(TAG, e.getMessage());
            resizedBitmap = null;
        }

        return resizedBitmap;
    }

    /**
     * Returns @Bitmap resized to newHeight and newWidth
     * OR resized by only newHeight if newWidth==0
     * OR resized by only newWidth if newHeight==0
     * according to aspect ratio
     *
     * @param context
     * @param bitmapResId
     * @param newHeight
     * @param newWidth
     * @return
     */
    public static Bitmap getResizedBitmapFromResources(final Context context, final int bitmapResId, final int newHeight, final int newWidth) {
        if (context == null || bitmapResId == 0)
            return null;
        return getResizedBitmapFromResources(context, bitmapResId, newHeight, newWidth, false);
    }

    public static Bitmap getResizedBitmapFromResources(final Context context, final int bitmapResId, final int newHeight, final int newWidth, final boolean isOptimistic) {
        if (context == null || bitmapResId == 0)
            return null;
        Resources resources = context.getResources();
        ContentResolver contentResolver = context.getContentResolver();
        return getResizedBitmapFromResources(resources, contentResolver, bitmapResId, newHeight, newWidth, isOptimistic);
    }

    /**
     * Returns resized
     *
     * @param resources
     * @param contentResolver
     * @param bitmapResId
     * @param newHeight
     * @param newWidth
     * @return
     */
    public static Bitmap getResizedBitmapFromResources(final Resources resources, final ContentResolver contentResolver, final int bitmapResId, final int newHeight, final int newWidth) {
        if (resources == null)
            return null;
        return getResizedBitmapFromResources(resources, contentResolver, bitmapResId, newHeight, newWidth, false);
    }

    public static Bitmap getResizedBitmapFromResources(final Resources resources, final ContentResolver contentResolver, final int bitmapResId, final int newHeight, final int newWidth, final boolean isOptimistic) {
        if (resources == null || contentResolver == null)
            return null;

        Bitmap bitmap = null;

        if (newHeight > 0 && newWidth > 0)
            bitmap = getBitmapFromResources(resources, contentResolver, bitmapResId, Math.max(newHeight, newWidth), isOptimistic);
        else if (newWidth > 0)
            bitmap = getBitmapFromResourcesWithMaxWidth(resources, contentResolver, bitmapResId, newWidth, isOptimistic);
        else
            bitmap = getBitmapFromResources(resources, contentResolver, bitmapResId, newHeight, isOptimistic);

        return getResizedBitmap(bitmap, newHeight, newWidth);
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

    public static Uri getUriOfBitmapFromResources(final Context context, final int mBitmapResId) {
        if (context == null || mBitmapResId == 0)
            return null;
        Resources resources = context.getResources();
        return getUriOfBitmapFromResources(resources, mBitmapResId);

    }

    public static Uri getUriOfBitmapFromResources(final Resources resources, final int mBitmapResId) {
        if (resources == null || mBitmapResId == 0)
            return null;
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(mBitmapResId) + '/' +
                resources.getResourceTypeName(mBitmapResId) + '/' +
                resources.getResourceEntryName(mBitmapResId));
        return uri;

    }

    public static Bitmap getBitmapFromResources(final Context context, final int mBitmapResId, final int sideSizeLimit) {
        if (context == null || mBitmapResId == 0)
            return null;
        return getBitmapFromResources(context, mBitmapResId, sideSizeLimit, false);
    }

    public static Bitmap getBitmapFromResources(final Context context, final int mBitmapResId, final int sideSizeLimit, final boolean isOptimistic) {
        if (context == null || mBitmapResId == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(context, mBitmapResId);
        return getBitmapByUriFromResources(context, uri, sideSizeLimit, isOptimistic);
    }

    public static Bitmap getBitmapFromResources(final Resources resources, final ContentResolver resolver, final int mBitmapResId, final int sideSizeLimit) {
        if (resources == null)
            return null;
        return getBitmapFromResources(resources, resolver, mBitmapResId, sideSizeLimit, false);
    }

    public static Bitmap getBitmapFromResources(final Resources resources, final ContentResolver resolver, final int mBitmapResId, final int sideSizeLimit, final boolean isOptimistic) {
        if (resources == null || resolver == null || mBitmapResId == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(resources, mBitmapResId);
        return getBitmapByUriFromResources(resolver, uri, sideSizeLimit, isOptimistic);
    }

    public static Bitmap getBitmapByUriFromResources(final Context context, final Uri uri, final int sideSizeLimit) {
        if (context == null || uri == null)
            return null;
        return getBitmapByUriFromResources(context, uri, sideSizeLimit, false);
    }

    public static Bitmap getBitmapByUriFromResources(final Context context, final Uri uri, final int sideSizeLimit, final boolean isOptimistic) {
        if (context == null || uri == null)
            return null;
        ContentResolver resolver = context.getContentResolver();
        return getBitmapByUriFromResources(resolver, uri, sideSizeLimit, isOptimistic);
    }

    public static Bitmap getBitmapByUriFromResources(final ContentResolver resolver, final Uri uri, final int sideSizeLimit) {
        return getBitmapByUriFromResources(resolver, uri, sideSizeLimit, false);
    }

    public static Bitmap getBitmapByUriFromResources(final ContentResolver resolver, final Uri uri, final int sideSizeLimit, final boolean isOptimistic) {
        if (resolver == null || uri == null)
            return null;

        Bitmap bitmap = null;
        InputStream inputStream = null;

        int maxWidth = 0, maxHeight = 0;
        if (sideSizeLimit > 0) {
            maxWidth = sideSizeLimit;
            maxHeight = sideSizeLimit;
        } else if (DeviceInfo.displayWidth > 0 && DeviceInfo.displayHeight > 0) {
            maxWidth = DeviceInfo.displayWidth;
            maxHeight = DeviceInfo.displayHeight;
        } else {
            maxWidth = DeviceInfo.getDeviceMaxSideSizeByDensity();
            maxHeight = maxWidth;
        }
        try {
            inputStream = resolver.openInputStream(uri);

            // decode image size
            final BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
            bmfOtions.inJustDecodeBounds = true;
            bmfOtions.inPurgeable = true;
            BitmapFactory.decodeStream(inputStream, null, bmfOtions);

            // Find the correct scale value. It should be the power of 2.
            // final int REQUIRED_SIZE=70;
            int width_tmp = bmfOtions.outWidth, height_tmp = bmfOtions.outHeight;
            int scale = 1;
            while (width_tmp > maxWidth || height_tmp > maxHeight) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }

            if (isOptimistic && scale > 1)
                scale--;

            bmfOtions.inSampleSize = scale;
            bmfOtions.inJustDecodeBounds = false;

            // decode with inSampleSize
            // BitmapFactory.Options o2 = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOtions);
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


    public static Bitmap getBitmapFromFileWithMaxHeight(final String bitmapFile, final int maxHeight) {
        return getBitmapFromFileWithMaxHeight(bitmapFile, maxHeight, false);
    }

    public static Bitmap getBitmapFromFileWithMaxHeight(final String bitmapFile, final int maxHeight, final boolean isOptimistic) {
        if (bitmapFile == null || bitmapFile.length() == 0)
            return null;
        return getBitmapFromFileWithMaxSideSize(new File(bitmapFile), maxHeight, true, isOptimistic);
    }

    public static Bitmap getBitmapFromFileWithMaxWidth(final String bitmapFile, final int maxWidth) {
        return getBitmapFromFileWithMaxWidth(bitmapFile, maxWidth, false);
    }

    public static Bitmap getBitmapFromFileWithMaxWidth(final String bitmapFile, final int maxWidth, final boolean isOptimistic) {
        if (bitmapFile == null || bitmapFile.length() == 0)
            return null;
        return getBitmapFromFileWithMaxSideSize(new File(bitmapFile), maxWidth, false, isOptimistic);
    }

    public static Bitmap getBitmapFromFileWithMaxHeight(final File bitmapFile, final int maxHeight) {
        return getBitmapFromFileWithMaxHeight(bitmapFile, maxHeight, false);
    }

    public static Bitmap getBitmapFromFileWithMaxHeight(final File bitmapFile, final int maxHeight, final boolean isOptimistic) {
        if (bitmapFile == null || maxHeight == 0)
            return null;
        return getBitmapFromFileWithMaxSideSize(bitmapFile, maxHeight, true, isOptimistic);
    }

    public static Bitmap getBitmapFromFileWithMaxWidth(final File bitmapFile, final int maxWidth) {
        return getBitmapFromFileWithMaxWidth(bitmapFile, maxWidth, false, false);
    }

    public static Bitmap getBitmapFromFileWithMaxWidth(final File bitmapFile, final int maxWidth, final boolean isOptimistic) {
        return getBitmapFromFileWithMaxWidth(bitmapFile, maxWidth, false, isOptimistic);
    }

    public static Bitmap getBitmapFromFileWithMaxWidth(final File bitmapFile, final int maxWidth, final boolean isByHeight, final boolean isOptimistic) {
        if (bitmapFile == null || maxWidth == 0)
            return null;
        return getBitmapFromFileWithMaxSideSize(bitmapFile, maxWidth, false, isOptimistic);
    }


    public static Bitmap getBitmapFromFileWithMaxSideSize(final File bitmapFile, final int maxSideSize, final boolean isByHeight) {
        return getBitmapFromFileWithMaxSideSize(bitmapFile, maxSideSize, isByHeight, false);
    }

    public static Bitmap getBitmapFromFileWithMaxSideSize(final File bitmapFile, final int maxSideSize, final boolean isByHeight, final boolean isOptimistic) {
        if (!FileUtils.isReadable(bitmapFile))
            return null;
        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        Bitmap bitmap = null;
        try {
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }
            if (fileDescriptor != null)
                bitmap = getBitmapFromStreamWithMaxSideSize(fileDescriptor, maxSideSize, isByHeight, isOptimistic);
            else
                bitmap = getBitmapFromStreamWithMaxSideSize(fileInputStream, maxSideSize, isByHeight, isOptimistic);
        } catch (FileNotFoundException e) {
            Log.e("ImageUtils", e);
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }

        return bitmap;
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

        int maxSideSize;
        boolean isByHeight;
        if (newHeight > newHeight) {
            maxSideSize = newHeight;
            isByHeight = true;
        } else {
            maxSideSize = newWidth;
            isByHeight = false;
        }

        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        Bitmap bitmap = null;
        try {
            fileInputStream = new FileInputStream(bitmapFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }
            if (fileDescriptor != null)
                bitmap = getBitmapFromStreamWithMaxSideSize(fileDescriptor, maxSideSize, isByHeight, isOptimistic);
            else
                bitmap = getBitmapFromStreamWithMaxSideSize(fileInputStream, maxSideSize, isByHeight, isOptimistic);
        } catch (FileNotFoundException e) {
            Log.e("ImageUtils", e);
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }
        if (newHeight == 0 || newWidth == 0) {
            final BitmapInfo bitmapInfo = new BitmapInfo(bitmap);
            if (newHeight == 0)
                newWidth = (int) bitmapInfo.getWidthByHeight(newHeight);
            else
                newWidth = (int) bitmapInfo.getHeightByWidth(newWidth);
        }

        return getResizedBitmap(bitmap, newHeight, newWidth);
    }

    public static Bitmap getBitmapFromResourcesWithMaxHeight(final Context context, final int mBitmapResId, final int maxHeight) {
        if (context == null || mBitmapResId == 0)
            return null;
        return getBitmapFromResourcesWithMaxHeight(context, mBitmapResId, maxHeight, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxHeight(final Context context, final int mBitmapResId, final int maxHeight, final boolean isOptimistic) {
        if (context == null || mBitmapResId == 0 || maxHeight == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(context, mBitmapResId);
        return getBitmapFromResourcesWithMaxSideSize(context, uri, maxHeight, true, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxHeight(final Resources resources, final ContentResolver resolver, final int mBitmapResId, final int maxHeight) {
        if (resources == null)
            return null;
        return getBitmapFromResourcesWithMaxHeight(resources, resolver, mBitmapResId, maxHeight, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxHeight(final Resources resources, final ContentResolver resolver, final int mBitmapResId, final int maxHeight, final boolean isOptimistic) {
        if (resolver == null || resources == null || mBitmapResId == 0 || maxHeight == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(resources, mBitmapResId);
        return getBitmapFromResourcesWithMaxSideSize(resolver, uri, maxHeight, true, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxWidth(final Context context, final int mBitmapResId, final int maxWidth) {
        if (context == null || mBitmapResId == 0)
            return null;
        return getBitmapFromResourcesWithMaxWidth(context, mBitmapResId, maxWidth, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxWidth(final Context context, final int mBitmapResId, final int maxWidth, final boolean isOptimistic) {
        if (context == null || mBitmapResId == 0 || maxWidth == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(context, mBitmapResId);
        return getBitmapFromResourcesWithMaxSideSize(context, uri, maxWidth, false, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxWidth(final Resources resources, final ContentResolver resolver, final int mBitmapResId, final int maxWidth) {
        if (resources == null)
            return null;
        return getBitmapFromResourcesWithMaxWidth(resources, resolver, mBitmapResId, maxWidth, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxWidth(final Resources resources, final ContentResolver resolver, final int mBitmapResId, final int maxWidth, final boolean isOptimistic) {
        if (resolver == null || resolver == null || mBitmapResId == 0 || maxWidth == 0)
            return null;
        Uri uri = getUriOfBitmapFromResources(resources, mBitmapResId);
        return getBitmapFromResourcesWithMaxSideSize(resolver, uri, maxWidth, false, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxSideSize(final Context context, final Uri uri, final int maxSideSize, final boolean isByHeight) {
        if (context == null || uri == null)
            return null;
        return getBitmapFromResourcesWithMaxSideSize(context, uri, maxSideSize, isByHeight, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxSideSize(final Context context, final Uri uri, final int maxSideSize, final boolean isByHeight, final boolean isOptimistic) {
        if (context == null || uri == null)
            return null;
        ContentResolver resolver = context.getContentResolver();
        return getBitmapFromResourcesWithMaxSideSize(resolver, uri, maxSideSize, isByHeight, isOptimistic);
    }

    public static Bitmap getBitmapFromResourcesWithMaxSideSize(final ContentResolver resolver, final Uri uri, final int maxSideSize, final boolean isByHeight) {
        return getBitmapFromResourcesWithMaxSideSize(resolver, uri, maxSideSize, isByHeight, false);
    }

    public static Bitmap getBitmapFromResourcesWithMaxSideSize(ContentResolver resolver, Uri uri, int maxSideSize, boolean isByHeight, boolean isOptimistic) {

        Bitmap bitmap = null;
        InputStream inputStream = null;

        try {
            inputStream = resolver.openInputStream(uri);
            bitmap = getBitmapFromStreamWithMaxSideSize(inputStream, maxSideSize, isByHeight, isOptimistic);
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

    public static Bitmap getBitmapFromStreamWithMaxSideSize(final InputStream inputStream, final float maxSideSize, final boolean isByHeight, final boolean isOptimistic) {
        if (maxSideSize == 0)
            return null;

        // decode image size
        final BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
        bmfOtions.inJustDecodeBounds = true;
        bmfOtions.inPurgeable = true;
        BitmapFactory.decodeStream(inputStream, null, bmfOtions);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = bmfOtions.outWidth;
        int height_tmp = bmfOtions.outHeight;
        int scale = 1;
        if (isByHeight)
            while (height_tmp > maxSideSize && (!isOptimistic || height_tmp / maxSideSize > 1.5f)) {
                height_tmp >>= 1;
                scale++;
            }
        else
            while (width_tmp > maxSideSize && (!isOptimistic || width_tmp / maxSideSize > 1.5f)) {
                width_tmp >>= 1;
                scale++;
            }

        bmfOtions.inSampleSize = scale;
        bmfOtions.inJustDecodeBounds = false;

        // decode with inSampleSize
        // BitmapFactory.Options o2 = new BitmapFactory.Options();
        Log.i(TAG, "getBitmapFromStreamWithMaxSideSize() bmfOtions.inSampleSize: " + scale);
        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmfOtions);
        return bitmap;
    }

    public static Bitmap getBitmapFromStreamWithMaxSideSize(final FileDescriptor fileDescriptor, final float maxSideSize, final boolean isByHeight, final boolean isOptimistic) {
        if (maxSideSize == 0)
            return null;

        // decode image size
        final BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
        bmfOtions.inJustDecodeBounds = true;
        bmfOtions.inPurgeable = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = bmfOtions.outWidth;
        int height_tmp = bmfOtions.outHeight;
        int scale = 1;
        if (isByHeight)
            while (height_tmp > maxSideSize && (!isOptimistic || height_tmp / maxSideSize > 1.5f)) {
                height_tmp >>= 1;
                scale++;
            }
        else
            while (width_tmp > maxSideSize && (!isOptimistic || width_tmp / maxSideSize > 1.5f)) {
                width_tmp >>= 1;
                scale++;
            }

        bmfOtions.inSampleSize = scale;
        bmfOtions.inJustDecodeBounds = false;

        // decode with inSampleSize
        // BitmapFactory.Options o2 = new BitmapFactory.Options();
        Log.i(TAG, "getBitmapFromStreamWithMaxSideSize() bmfOtions.inSampleSize: " + scale);
        final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
        return bitmap;
    }

    public static Bitmap getBitmapFromResources(final Context context, final int resId) {
        if (context == null || resId == 0)
            return null;

        return getBitmapFromResources(context.getResources(), resId);
    }

    public static Bitmap getBitmapFromResources(final Resources resources, final int imageResId) {
        if (resources == null || imageResId == 0)
            return null;

        return BitmapFactory.decodeResource(resources, imageResId);
    }

    public static Drawable getDrawableFromBitmap(final Bitmap bitmap, final Context context) {
        if (bitmap == null || context == null)
            return null;

        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);

        return drawable;
    }


    /**
     * Returns @Bitmap being set to given @ImageView
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
     * This method returns a @Bitmap related to given @Drawable.
     *
     * @param drawable @Drawable resource of image
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

    public static Bitmap getBitmapByAddImages(final Context context, final Bitmap mBitmap1, final int mBitmap2ResId) {
        if (context == null || mBitmap1 == null)
            return null;
        final Bitmap mBitmap2 = getBitmapFromResources(context, mBitmap2ResId);
        final Bitmap result = getBitmapByAddImages(context, mBitmap1, mBitmap2);
        //mBitmap2.recycle();
        return result;
    }

    public static Bitmap getBitmapByAddImages(final Context context, final int mBitmap1ResId, final Bitmap mBitmap2) {
        if (context == null || mBitmap2 == null)
            return null;
        final Bitmap mBitmap1 = getBitmapFromResources(context, mBitmap1ResId);
        final Bitmap result = getBitmapByAddImages(context, mBitmap1, mBitmap2);
        //mBitmap1.recycle();
        return result;
    }

    public static Bitmap getBitmapByMergeImagesToTheRight(final Context context, final Bitmap mLeftBitmap, final Bitmap mRightBitmap) {
        if (context == null || mLeftBitmap == null && mRightBitmap == null)
            return null;
        if (mRightBitmap == null)
            return mLeftBitmap;
        if (mLeftBitmap == null)
            return mRightBitmap;

        int m_width = 0, m_height = 0;
        if (mLeftBitmap.getWidth() > mRightBitmap.getWidth()) {
            m_width = mLeftBitmap.getWidth();
            m_height = mLeftBitmap.getHeight() + mRightBitmap.getHeight();
        } else {
            m_width = mRightBitmap.getWidth();
            m_height = mLeftBitmap.getHeight() + mRightBitmap.getHeight();
        }
        final Bitmap m_combinedImages = Bitmap.createBitmap(m_width, m_height, Bitmap.Config.ARGB_8888);
        final Canvas m_comboImage = new Canvas(m_combinedImages);

        m_comboImage.drawBitmap(mLeftBitmap, 0f, 0f, null);
        m_comboImage.drawBitmap(mRightBitmap, mLeftBitmap.getWidth(), 0f, null);
        return m_combinedImages;
    }

    public static Bitmap getBitmapByAddImages(final Context context, Bitmap mBitmap1, final Bitmap mBitmap2) {
        if (context == null || mBitmap1 == null && mBitmap2 == null)
            return null;
        if (mBitmap2 == null)
            return mBitmap1;
        if (mBitmap1 == null)
            return mBitmap2;


//		if (srcBmp.getWidth() > srcBmp.getHeight()){
//
//			  dstBmp = Bitmap.createBitmap(
//			     srcBmp, 
//			     srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
//			     0,
//			     srcBmp.getHeight(), 
//			     srcBmp.getHeight()
//			     );
//
//		}else{
//
//			  dstBmp = Bitmap.createBitmap(
//			     srcBmp,
//			     0, 
//			     srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
//			     srcBmp.getWidth(),
//			     srcBmp.getWidth() 
//			     );
//			}

        int m_width = 0, m_height = 0;
        if (mBitmap1.getWidth() > mBitmap2.getWidth()) {
            m_width = mBitmap2.getWidth();
            m_height = mBitmap2.getHeight();
        } else {
            m_width = mBitmap1.getWidth();
            m_height = mBitmap1.getHeight();
        }
        final Bitmap m_combinedImages = Bitmap.createBitmap(m_width, m_height, Bitmap.Config.ARGB_8888);
        final Canvas m_comboImage = new Canvas(m_combinedImages);
        m_comboImage.drawBitmap(mBitmap1, 0f, 0f, null);
        m_comboImage.drawBitmap(mBitmap2, 0f, 0f, null);
        return m_combinedImages;
    }

    public static Bitmap makePreviewAndSave(File fullsizedImageFile, File previewImageFile, int maxAllowedSideSize) {
        return makePreviewAndSave(fullsizedImageFile, previewImageFile, maxAllowedSideSize, 90);
    }

    public static Bitmap makePreviewAndSave(File fullsizedImageFile, File previewImageFile, int maxAllowedSideSize, int maxAllowedQuality) {
        if (!FileUtils.isReadable(fullsizedImageFile)
                || fullsizedImageFile.length() == 0 || previewImageFile == null
                || previewImageFile.exists() && !previewImageFile.canWrite()
                || maxAllowedSideSize == 0)
            return null;

//		if (previewImageFile.exists() && !previewImageFile.delete())
//			return false;

        Bitmap bitmap = getPreviewBitmapFromFile(fullsizedImageFile, maxAllowedSideSize);
        if (bitmap == null)
            return null;

        saveBitmapToJPEGFile(bitmap, previewImageFile, maxAllowedQuality);

        //bitmap.recycle();

        return bitmap;
    }

    public static Bitmap getPreviewBitmapFromFile(File fullsizedImageFile, int maxAllowedSideSize) {
        return getPreviewBitmapFromFile(fullsizedImageFile, maxAllowedSideSize, false, false, false);
    }

    public static Bitmap getPreviewBitmapFromFile(File fullsizedImageFile, int maxAllowedSideSize, boolean doLessScale, boolean doStretch, boolean doKeepAspectRatioWhileStrech) {

        if (!FileUtils.isReadable(fullsizedImageFile)
                || fullsizedImageFile.length() == 0
                || maxAllowedSideSize == 0 && (DeviceInfo.displayWidth == 0 || DeviceInfo.displayHeight == 0))
            return null;

        // надо ли даунсайзнуть имагу?
        //decode image size
        BitmapFactory.Options bmfOtions = new BitmapFactory.Options();
        bmfOtions.inJustDecodeBounds = true;
        bmfOtions.inPurgeable = true;
        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        try {
            fileInputStream = new FileInputStream(fullsizedImageFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (fileDescriptor != null)
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
            else
                BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
        } catch (FileNotFoundException e) {
            Log.e("ImageUtils", e);
            return null;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }

        if (maxAllowedSideSize == 0) {
            int maxDeviceWidth = DeviceInfo.displayWidth / 4;
            int maxDeviceHeight = DeviceInfo.displayHeight / 4;
            maxAllowedSideSize = maxDeviceWidth < maxDeviceHeight ? maxDeviceWidth : maxDeviceHeight;
        }

        //Find the correct scale value. It should be the power of 2.
        int width_tmp = bmfOtions.outWidth, height_tmp = bmfOtions.outHeight, scale = 1;
        while (width_tmp > maxAllowedSideSize || height_tmp > maxAllowedSideSize) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale++;
        }

//        Log.i("makePreviewAndSave", "bmfOtions.outWidth: "+bmfOtions.outWidth+" bmfOtions.outHeight: "+bmfOtions.outHeight+" maxAllowedSideSize: "+maxAllowedSideSize);
//        Log.i("makePreviewAndSave", "width_tmp: "+width_tmp+" height_tmp: "+height_tmp+" scale: "+scale);

        Bitmap bitmap = null;

        // nothing to do if scale==1 and no quality reduce needed
        if (scale == 1) {
            try {
                fileInputStream = new FileInputStream(fullsizedImageFile);
                try {
                    fileDescriptor = fileInputStream.getFD();
                } catch (IOException ignored) {
                }

                if (fileDescriptor != null)
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
                else
                    bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
            } catch (FileNotFoundException e) {
                Log.e("ImageUtils", e);
                return null;
            } finally {
                if (fileInputStream != null)
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
            }
            return bitmap; // возвращаем пезультат - конец метода!!!
        }

        //увеличиваем scale-фактор, если по одной из сторон более чем в 2 раза меньше размер 
        if (doLessScale && (width_tmp * 2 < maxAllowedSideSize || height_tmp * 2 < maxAllowedSideSize))
            scale--;

        // down scaling the image
        bmfOtions.inSampleSize = scale;
        bmfOtions.inJustDecodeBounds = false;
        bmfOtions.inPurgeable = true;

        try {
            fileInputStream = new FileInputStream(fullsizedImageFile);
            try {
                fileDescriptor = fileInputStream.getFD();
            } catch (IOException ignored) {
            }

            if (doStretch) {
                if (fileDescriptor != null)
                    bitmap = getScaledBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions), maxAllowedSideSize, doKeepAspectRatioWhileStrech);
                else
                    bitmap = getScaledBitmap(BitmapFactory.decodeStream(fileInputStream, null, bmfOtions), maxAllowedSideSize, doKeepAspectRatioWhileStrech);
            } else {
                if (fileDescriptor != null)
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmfOtions);
                else
                    bitmap = BitmapFactory.decodeStream(fileInputStream, null, bmfOtions);
            }

        } catch (FileNotFoundException e) {
            Log.e("ImageUtils", e);
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
        }

        return bitmap;
    }


    public static boolean saveBitmapToJPEGFile(final Bitmap bitmap, final File imageFile, int maxAllowedQuality) {

        if (bitmap == null || imageFile == null || imageFile.exists() && !imageFile.canWrite())
            return false;

        if (imageFile.exists() && !imageFile.delete())
            return false;

        if (maxAllowedQuality > 100 || maxAllowedQuality < 0)
            maxAllowedQuality = 100;

        boolean isSucceed = false;

        // save it
        // Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (maxAllowedQuality == 0)
            bitmap.compress(CompressFormat.JPEG, 100, bos);
        else
            bitmap.compress(CompressFormat.JPEG, maxAllowedQuality, bos);

        // write the bytes in file
        isSucceed = FileUtils.byteArrayOutputStreamToFile(bos, imageFile);

        if (bos != null)
            try {
                bos.close();
            } catch (IOException e) {
            }

        return isSucceed;
    }


    public static boolean saveBitmapToPNGFile(final Bitmap bitmap, final File imageFile) {
        if (bitmap == null || imageFile == null || imageFile.exists() && !imageFile.canWrite())
            return false;

        if (imageFile.exists() && !imageFile.delete())
            return false;

        boolean isSucceed = false;

        // save it
        // Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

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
     * Deletes a copy of a photo which could be created on several devices
     * while using camera
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


    public static Bitmap getBluredBitmap(final Resources res, final int imageId, final int radius) {
        Bitmap sentBitmap = BitmapFactory.decodeResource(res, imageId);
        return getBluredBitmap(res, sentBitmap, radius);
    }

    public static Bitmap getBluredBitmap(final Resources res, final Bitmap sentBitmap, final int radius) {

        final Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        // Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

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
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
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
                yi = Math.max(0, yp) + x;

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

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


    /**
     * Creates snapshot handler that captures the root of the whole activity.
     *
     * @param activity
     * @return @Bitmap
     */
    public static Bitmap getScreenShotOfActivity(final Activity activity) {
        final View view = activity.findViewById(android.R.id.content);
        return getScreenShotOfView(view);
    }

    /**
     * Creates snapshot handler that captures the view with target id of the activity
     *
     * @param activity
     * @param id
     * @return @Bitmap
     */
    public static Bitmap getScreenShotOfActivityView(final Activity activity, final int id) {
        return getScreenShotOfView(activity.findViewById(id));
    }

    /**
     * Takes a snapshot of the view.
     *
     * @param view
     * @return @Bitmap
     */
    public static Bitmap getScreenShotOfView(final View view) {
        if (view == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap getScreenShotOfView(final View view, final int width, final int height) {
        if (view == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    public static Bitmap getBitmapByCropFromCenterWithScaling(final File bitmapFile, int cropToHeight, int cropToWidth) {
        if (bitmapFile == null || !FileUtils.isReadable(bitmapFile) || cropToHeight == 0 || cropToWidth == 0)
            return null;

        final BitmapInfo bitmapInfo = ImageUtils.getBitmapInfoFromFile(bitmapFile);
        if (bitmapInfo == null)
            return null;

//		while (bitmapInfo.width<cropToWidth || bitmapInfo.height<cropToHeight)
//			bitmapInfo = new BitmapInfo(bitmapInfo.width*2, bitmapInfo.height*2);

        float bitmapWHProportions = (float) bitmapInfo.width / (float) bitmapInfo.height;
        float bitmapHWProportions = (float) bitmapInfo.height / (float) bitmapInfo.width;
        float cropWHProportions = (float) cropToWidth / (float) cropToHeight;

        Bitmap bitmapToCrop = null;
        // crop Portrait from Portrait
        if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropToHeight > cropToWidth) {
            // 480x800 -> 300x400, result is wider than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropToWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropToWidth;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
            } else {
                // scale by height
                if (cropToHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropToHeight;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropToWidth > cropToHeight) {
            // 800x480 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropToWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropToWidth;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
            } else {
                // scale by height
                if (cropToHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropToHeight;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Portrait
        else if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropToHeight < cropToWidth) {
            // 480x800 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            // scale by width
            if (cropToWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropToWidth;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            } else if (cropToHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropToHeight;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            }
            Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
        }
        // crop Portrait from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropToWidth < cropToHeight /*&& bitmapInfo.height>cropToHeight*/) {
            // scale by height
            if (cropToHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropToHeight;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            } else if (cropToWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropToWidth;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            }
            Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
        } else {
            if (bitmapInfo.hasLandscapeOrientation)
                bitmapToCrop = getBitmapFromFileWithMaxWidth(bitmapFile, (int) bitmapInfo.width);
            else
                bitmapToCrop = getBitmapFromFileWithMaxHeight(bitmapFile, (int) bitmapInfo.height);
        }

        Log.d("ImageUtils", "original bitmap H: " + bitmapInfo.height + " W: " + bitmapInfo.width);
        Log.d("ImageUtils", "cropToHeight: " + cropToHeight + " cropToWidth: " + cropToWidth);
        Log.d("ImageUtils", "bitmapToCrop h: " + bitmapToCrop.getHeight() + " w: " + bitmapToCrop.getWidth());

        if (bitmapToCrop.getWidth() > cropToWidth && bitmapToCrop.getHeight() > cropToHeight) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, deltaY,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getWidth() >= cropToWidth) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, 0,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getHeight() >= cropToHeight) {
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    0, deltaY,
                    cropToWidth, cropToHeight);
        } else
            return bitmapToCrop;
    }

    public static Bitmap getBitmapByCropFromCenterWithScaling(final Context context, final int bitmapResId, final int cropToHeight, final int cropToWidth) {
        if (context == null || bitmapResId == 0)
            return null;
        ContentResolver contentResolver = context.getContentResolver();
        Resources resources = context.getResources();
        return getBitmapByCropFromCenterWithScaling(resources, contentResolver, bitmapResId, cropToHeight, cropToWidth);
    }

    public static Bitmap getBitmapByCropFromCenterWithScaling(final Resources resources, final ContentResolver contentResolver, final int bitmapResId, int cropToHeight, int cropToWidth) {
        if (resources == null || contentResolver == null || bitmapResId == 0 || cropToHeight == 0 && cropToWidth == 0) {
            Log.d("ImageUtils", "getBitmapByCropFromCenterWithScaling() fucked off by params");
            return null;
        }

        final BitmapInfo bitmapInfo = ImageUtils.getBitmapInfoFromResources(resources, contentResolver, bitmapResId);
        if (bitmapInfo == null) {
            Log.d("ImageUtils", "getBitmapByCropFromCenterWithScaling() fucked off by bitmapInfo == null");
            return null;
        }

        float bitmapWHProportions = (float) bitmapInfo.width / (float) bitmapInfo.height;
        float bitmapHWProportions = (float) bitmapInfo.height / (float) bitmapInfo.width;
        float cropWHProportions = (float) cropToWidth / (float) cropToHeight;

        Bitmap bitmapToCrop = null;
        // crop Portrait from Portrait
        if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropToHeight > cropToWidth) {
            // 480x800 -> 300x400, result is wider than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropToWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropToWidth;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxWidth(resources, contentResolver, bitmapResId, cropToWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
            } else {
                // scale by height
                if (cropToHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropToHeight;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxHeight(resources, contentResolver, bitmapResId, cropToHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropToWidth > cropToHeight) {
            // 800x480 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropToWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropToWidth;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxWidth(resources, contentResolver, bitmapResId, cropToWidth);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
            } else {
                // scale by height
                if (cropToHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropToHeight;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxHeight(resources, contentResolver, bitmapResId, cropToHeight);
                if (bitmapToScale == null)
                    return null;
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Portrait
        else if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropToHeight < cropToWidth && bitmapInfo.width > cropToWidth) {
            // 480x800 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            // scale by width
            if (cropToWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropToWidth;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            } else if (cropToHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropToHeight;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            }
            final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxWidth(resources, contentResolver, bitmapResId, cropToWidth);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
        }
        // crop Portrait from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropToWidth < cropToHeight /*&& bitmapInfo.height>cropToHeight*/) {
            // scale by height
            if (cropToHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropToHeight;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            } else if (cropToWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropToWidth;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            }
            final Bitmap bitmapToScale = getBitmapFromResourcesWithMaxHeight(resources, contentResolver, bitmapResId, cropToHeight);
            if (bitmapToScale == null)
                return null;
            bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
        } else {
            if (bitmapInfo.hasLandscapeOrientation)
                bitmapToCrop = getBitmapFromResourcesWithMaxWidth(resources, contentResolver, bitmapResId, (int) bitmapInfo.width);
            else
                bitmapToCrop = getBitmapFromResourcesWithMaxHeight(resources, contentResolver, bitmapResId, (int) bitmapInfo.height);
        }


        Log.d("ImageUtils", "original bitmap H: " + bitmapInfo.height + " W: " + bitmapInfo.width);
        Log.d("ImageUtils", "cropToHeight: " + cropToHeight + " cropToWidth: " + cropToWidth);
        Log.d("ImageUtils", "bitmapToCrop h: " + bitmapToCrop.getHeight() + " w: " + bitmapToCrop.getWidth());


        if (bitmapToCrop.getWidth() > cropToWidth && bitmapToCrop.getHeight() > cropToHeight) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, deltaY,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getWidth() >= cropToWidth) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, 0,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getHeight() >= cropToHeight) {
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    0, deltaY,
                    cropToWidth, cropToHeight);
        } else
            return bitmapToCrop;
    }


    public static Bitmap getBitmapByCropFromCenterWithScaling(final Bitmap bitmapToScale, int cropToHeight, int cropToWidth) {
        if (bitmapToScale == null || cropToHeight == 0 || cropToWidth == 0)
            return null;

        Log.d("ImageUtils", "using Bitmap. original cropToHeight: " + cropToHeight + " cropToWidth: " + cropToWidth);

        BitmapInfo bitmapInfo = new BitmapInfo(bitmapToScale.getWidth(), bitmapToScale.getHeight());

        float bitmapWHProportions = (float) bitmapInfo.width / (float) bitmapInfo.height;
        float bitmapHWProportions = (float) bitmapInfo.height / (float) bitmapInfo.width;
        float cropWHProportions = (float) cropToWidth / (float) cropToHeight;

        Bitmap bitmapToCrop = null;
        // crop Portrait from Portrait
        if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropToHeight > cropToWidth) {
            // 480x800 -> 300x400, result is wider than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropToWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropToWidth;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }

                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
            } else {
                // scale by height
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
                if (cropToHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropToHeight;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropToWidth > cropToHeight) {
            // 800x480 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            if (bitmapWHProportions <= cropWHProportions) {
                // scale by width
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
                if (cropToWidth > bitmapInfo.width) {
                    float downSample = bitmapInfo.width / cropToWidth;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
            } else {
                // scale by height
                //Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
                if (cropToHeight > bitmapInfo.height) {
                    float downSample = bitmapInfo.height / cropToHeight;
                    cropToWidth *= downSample;
                    cropToHeight *= downSample;
                }
                bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
            }
        }
        // crop Landscape from Portrait
        else if ((bitmapInfo.hasPortraitOrientation || bitmapInfo.hasSquareForm) && cropToHeight < cropToWidth /*&& bitmapInfo.width>cropToWidth*/) {
            // 480x800 -> 400x300, 1.66 -> 1.33, result is higher than original bitmap
            // scale by width
            //Bitmap bitmapToScale = getBitmapFromFileWithMaxWidth(bitmapFile, cropToWidth);
            if (cropToWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropToWidth;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            } else if (cropToHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropToHeight;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            }
            bitmapToCrop = getResizedBitmap(bitmapToScale, (int) (cropToWidth * bitmapHWProportions), cropToWidth);
        }
        // crop Portrait from Landscape
        else if ((bitmapInfo.hasLandscapeOrientation || bitmapInfo.hasSquareForm) && cropToWidth < cropToHeight /*&& bitmapInfo.height>cropToHeight*/) {
            // scale by height
            //Bitmap bitmapToScale = getBitmapFromFileWithMaxHeight(bitmapFile, cropToHeight);
            if (cropToHeight > bitmapInfo.height) {
                float downSample = bitmapInfo.height / cropToHeight;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            } else if (cropToWidth > bitmapInfo.width) {
                float downSample = bitmapInfo.width / cropToWidth;
                cropToWidth *= downSample;
                cropToHeight *= downSample;
            }
            bitmapToCrop = getResizedBitmap(bitmapToScale, cropToHeight, (int) (cropToHeight * bitmapWHProportions));
        } else {
            bitmapToCrop = bitmapToScale;
        }

        Log.d("ImageUtils", "original bitmap H: " + bitmapInfo.height + " W: " + bitmapInfo.width);
        Log.d("ImageUtils", "cropToHeight: " + cropToHeight + " cropToWidth: " + cropToWidth);
        Log.d("ImageUtils", "bitmapToCrop h: " + bitmapToCrop.getHeight() + " w: " + bitmapToCrop.getWidth());

        if (bitmapToCrop.getWidth() > cropToWidth && bitmapToCrop.getHeight() > cropToHeight) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, deltaY,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getWidth() >= cropToWidth) {
            int deltaX = (bitmapToCrop.getWidth() - cropToWidth) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    deltaX, 0,
                    cropToWidth, cropToHeight);
        } else if (bitmapToCrop.getHeight() >= cropToHeight) {
            int deltaY = (bitmapToCrop.getHeight() - cropToHeight) / 2;
            return Bitmap.createBitmap(bitmapToCrop,
                    0, deltaY,
                    cropToWidth, cropToHeight);
        } else
            return bitmapToCrop;

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void setBackground(final View view, final Bitmap bitmap) {
        if (view == null || bitmap == null)
            return;
        if (DeviceInfo.hasAPI16())
            view.setBackground(ImageUtils.getDrawableFromBitmap(bitmap, view.getContext()));
        else
            view.setBackgroundDrawable(ImageUtils.getDrawableFromBitmap(bitmap, view.getContext()));
    }

    public static void setBackground(View view, int resId) {
        if (view == null || resId == 0)
            return;
        view.setBackgroundResource(resId);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void setBackground(final View view, final BitmapDrawable bitmap) {
        if (view == null || bitmap == null)
            return;
        if (DeviceInfo.hasAPI16())
            view.setBackground(bitmap);
        else
            view.setBackgroundDrawable(bitmap);

    }


    public static byte[] getJPEGByteArrayFromBitmap(final Bitmap bitmap, final int jpegQuality) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, jpegQuality, baos);
        return baos.toByteArray();
    }

    public static byte[] getJPEGByteArrayFromFile(final String imageFile, final int jpegQuality) {
        return getJPEGByteArrayFromFile(new File(imageFile), jpegQuality);
    }

    public static byte[] getJPEGByteArrayFromFile(final File image, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getBitmapFromFile(image);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    public static byte[] getJPEGByteArrayFromFile(final String imageFile, final int maxSideSize, final int jpegQuality) {
        return getJPEGByteArrayFromFile(new File(imageFile), maxSideSize, jpegQuality);
    }

    public static byte[] getJPEGByteArrayFromFile(final File image, final int maxSideSize, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getPreviewBitmapFromFile(image, maxSideSize);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    public static byte[] getJPEGByteArrayFromResources(final Context context, final int imageResId, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getBitmapFromResources(context, imageResId, 0);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    public static byte[] getJPEGByteArrayFromResources(final Context context, final int imageResId, final int maxSideSize, final int jpegQuality) {
        final Bitmap bm = ImageUtils.getResizedBitmapFromResources(context, imageResId, maxSideSize, maxSideSize);
        final byte[] result = getJPEGByteArrayFromBitmap(bm, jpegQuality);
        bm.recycle();
        return result;
    }

    public static byte[] getPNGByteArrayFromBitmap(final Bitmap bitmap) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, baos);
        return baos.toByteArray();
    }

    public static byte[] getPNGByteArrayFromFile(final String imageFile) {
        return getPNGByteArrayFromFile(new File(imageFile));
    }

    public static byte[] getPNGByteArrayFromFile(final File image) {
        final Bitmap bm = ImageUtils.getBitmapFromFile(image);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    public static byte[] getPNGByteArrayFromFile(final String imageFile, final int maxSideSize) {
        return getPNGByteArrayFromFile(new File(imageFile), maxSideSize);
    }

    public static byte[] getPNGByteArrayFromFile(final File image, final int maxSideSize) {
        final Bitmap bm = ImageUtils.getPreviewBitmapFromFile(image, maxSideSize);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    public static byte[] getPNGByteArrayFromResources(final Context context, final int imageResId) {
        final Bitmap bm = ImageUtils.getBitmapFromResources(context, imageResId, 0);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }

    public static byte[] getPNGByteArrayFromResources(final Context context, final int imageResId, final int maxSideSize) {
        final Bitmap bm = ImageUtils.getResizedBitmapFromResources(context, imageResId, maxSideSize, maxSideSize);
        final byte[] result = getPNGByteArrayFromBitmap(bm);
        bm.recycle();
        return result;
    }


    public static Bitmap getRoundBitmap(final Bitmap bitmap) {
        return getCircleWrappedBitmap(bitmap);
    }

    public static Bitmap getRoundBitmap(final Bitmap bitmap, final int diameter) {
        return getCircleWrappedBitmap(bitmap, diameter);
    }

    public static Bitmap getCircleBitmap(final Bitmap bitmap) {
        return getCircleWrappedBitmap(bitmap);
    }

    public static Bitmap getCircleBitmap(final Bitmap bitmap, final int diameter) {
        return getCircleWrappedBitmap(bitmap, diameter);
    }

    public static Bitmap getCircleWrappedBitmap(final Bitmap bitmap) {
        if (bitmap == null)
            return null;
        return getCircleWrappedBitmap(bitmap, Math.min(bitmap.getWidth(), bitmap.getHeight()));
    }

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


    public static Bitmap getBlackAndWhiteBitmap(final Bitmap orginalBitmap) {
        return getBlackAndWhiteBitmap(orginalBitmap, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap getBlackAndWhiteBitmapARGB8888(final Bitmap orginalBitmap) {
        return getBlackAndWhiteBitmap(orginalBitmap, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap getBlackAndWhiteBitmapRGB565(final Bitmap orginalBitmap) {
        return getBlackAndWhiteBitmap(orginalBitmap, Bitmap.Config.RGB_565);
    }

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


    public static Bitmap getRotatedBitmapByAngle(final Bitmap bitmap, final float angle) {
        final Matrix matrix = new Matrix();
        matrix.postRotate(angle, bitmap.getWidth() / 2, bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public String getBase64EncodedImage(final File imageFile) {
        return FileUtils.getBase64EncodedFile(imageFile);
    }

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

//    public static Drawable getRotatedDrawable(final Bitmap b, final float angle) {
//	    final BitmapDrawable drawable = new BitmapDrawable(getResources(), b) {
//	        @Override
//	        public void draw(final Canvas canvas) {
//	            canvas.save();
//	            canvas.rotate(angle, b.getWidth() / 2, b.getHeight() / 2);
//	            super.draw(canvas);
//	            canvas.restore();
//	        }
//	    };
//	    return drawable;
//	}


}
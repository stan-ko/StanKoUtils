/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.stanko.image;

import java.io.FileDescriptor;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.stanko.tools.Log;
import com.stanko.tools.SdkBuildUtils;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class ImageResizer extends ImageWorker {
//    private static final String TAG = "ImageResizer";
    protected int mImageWidth;
    protected int mImageHeight;

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        setImageSize(imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageResizer(Context context, int imageSize) {
        super(context);
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     *
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     *
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    public int[] getImageSize() {
        return new int[]{mImageHeight, mImageWidth};
    }
    
    /**
     * The main processing method. This happens in a background task. In this case we are just
     * sampling down the bitmap and returning it from a resource.
     *
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId) {
//        Log.d(TAG, "processBitmap - " + resId);
        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth, mImageHeight, getImageCache());
    }

    @Override
    protected Bitmap processBitmap(Object data) {
//    	Log.d(this, "processBitmap(Object data) as int!!!!");
        return processBitmap(Integer.parseInt(String.valueOf(data)));
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (SdkBuildUtils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (SdkBuildUtils.hasHoneycomb())
            addInBitmapOptions(options, cache);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static synchronized Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {


        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try{
           	BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        } catch (IllegalArgumentException e) {//
        	Log.e("ImageResizer", e);
        	return null; //new Bitmap();
        } 
		
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (SdkBuildUtils.hasHoneycomb())
            addInBitmapOptions(options, cache);
        
        //Caused by: java.lang.IllegalArgumentException: Problem decoding into existing bitmap
        //android.graphics.BitmapFactory.decodeFileDescriptor(BitmapFactory.java:576)
        //android.image.utils.ImageResizer.decodeSampledBitmapFromDescriptor(ImageResizer.java:205)
        try{
        	return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        } catch (IllegalArgumentException e) {
        	android.util.Log.e("ImageResizer", e.toString());
        	return null; //new Bitmap();
        }
    }

    public static synchronized Bitmap decodeSampledBitmapFromStream(InputStream inputStream, int reqWidth, int reqHeight, ImageCache cache) {


        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try{
           	BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IllegalArgumentException e) {//
        	Log.e("ImageResizer", e);
        	return null; //new Bitmap();
        } 
		
        // Calculate inSampleSize
        final int sampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        Log.i("ImageResizer","decodeSampledBitmapFromStream() options.outHeight: "+options.outHeight+" options.outWidth: "+options.outWidth+"sampleSize: "+sampleSize);
        options.inSampleSize = sampleSize; 

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        //options.inPurgeable = true;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (SdkBuildUtils.hasHoneycomb())
            addInBitmapOptions(options, cache);
        
        //Caused by: java.lang.IllegalArgumentException: Problem decoding into existing bitmap
        //android.graphics.BitmapFactory.decodeFileDescriptor(BitmapFactory.java:576)
        //android.image.utils.ImageResizer.decodeSampledBitmapFromDescriptor(ImageResizer.java:205)
        //FlushedInputStream flushedInputStream = null;
        try{
        	//flushedInputStream = new FlushedInputStream(inputStream);
        	return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IllegalArgumentException e) {
        	Log.e("ImageResizer", e.toString());
        }
//        finally {
//        	if (flushedInputStream != null)
//				try {
//					flushedInputStream.close();
//				} catch (IOException e) {}
//        }
        
    	return null; //new Bitmap();
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
//                    Log.d(TAG, "Found bitmap to use for inBitmap");
                options.inBitmap = inBitmap;
            }
        }
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options An options object with out* params already populated (run through a decode*
     *            method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    
//    static class FlushedInputStream extends FilterInputStream {
//    	public FlushedInputStream(InputStream inputStream) {
//    	    super(inputStream);
//    	}
//
//        @Override
//        public int read(byte[] buffer, int offset, int count) throws IOException {
//            int ret = super.read(buffer, offset, count);
//            for ( int i = 6; i < buffer.length - 4; i++ ) {
//                if ( buffer[i] == 0x2c ) {
//                    if ( buffer[i + 2] == 0 && buffer[i + 1] > 0 && buffer[i + 1] <= 48 ) {
//                        buffer[i + 1] = 0;
//                    }
//                    if ( buffer[i + 4] == 0 && buffer[i + 3] > 0 && buffer[i + 3] <= 48 ) {
//                        buffer[i + 3] = 0;
//                    }
//                }
//            }
//            return ret;
//        }
//        
//    	@Override
//    	public long skip(long n) throws IOException {
//    	    long totalBytesSkipped = 0L;
//    	    while (totalBytesSkipped < n) {
//    	        long bytesSkipped = in.skip(n - totalBytesSkipped);
//    	        if (bytesSkipped == 0L) {
//    	              int readedByte = read();
//    	              if (readedByte < 0) {
//    	                  break;  // we reached EOF
//    	              } else {
//    	                  bytesSkipped = 1; // we read one byte
//    	              }
//    	       }
//    	        totalBytesSkipped += bytesSkipped;
//    	    }
//    	    return totalBytesSkipped;
//    	}
//    	
//    }
}

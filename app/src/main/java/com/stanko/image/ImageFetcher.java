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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import com.stanko.tools.DeviceInfo;
import com.stanko.tools.FileUtils;
import com.stanko.tools.Log;
import com.stanko.tools.SDCardHelper;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
	
    private static final String TAG = "ImageFetcher";
//    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    //private static final String HTTP_CACHE_DIR = "http";
    //private static final int IO_BUFFER_SIZE = 8 * 1024;

//    private DiskLruCache mHttpDiskCache;
//    private File mHttpCacheDir;
//    private boolean mHttpDiskCacheStarting = true;
//    private final Object mHttpDiskCacheLock = new Object();
//    private static final int DISK_CACHE_INDEX = 0;

    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
        //mHttpCacheDir = SDCardHelper.getCacheDir();//ImageCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
    }

    @Override
    protected void initDiskCacheInternal() {
        super.initDiskCacheInternal();
        initHttpDiskCache();
    }

    private void initHttpDiskCache() {
//        if (!mHttpCacheDir.exists()) {
//            mHttpCacheDir.mkdirs();
//        }
//        synchronized (mHttpDiskCacheLock) {
//            if (ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
//                try {
//                    mHttpDiskCache = DiskLruCache.open(mHttpCacheDir, 1, 1, HTTP_CACHE_SIZE);
//                    if (BuildConfig.DEBUG) {
//                        Log.d(TAG, "HTTP cache initialized");
//                    }
//                } catch (IOException e) {
//                    mHttpDiskCache = null;
//                }
//            }
//            mHttpDiskCacheStarting = false;
//            mHttpDiskCacheLock.notifyAll();
//        }
    }

    @Override
    protected void clearCacheInternal() {
//        super.clearCacheInternal();
//        synchronized (mHttpDiskCacheLock) {
//            if (mHttpDiskCache != null && !mHttpDiskCache.isClosed()) {
//                try {
//                    mHttpDiskCache.delete();
//                    if (BuildConfig.DEBUG) {
//                        Log.d(TAG, "HTTP cache cleared");
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "clearCacheInternal - " + e);
//                }
//                mHttpDiskCache = null;
//                mHttpDiskCacheStarting = true;
//                initHttpDiskCache();
//            }
//        }
    }

    @Override
    protected void flushCacheInternal() {
//        super.flushCacheInternal();
//        synchronized (mHttpDiskCacheLock) {
//            if (mHttpDiskCache != null) {
//                try {
//                    mHttpDiskCache.flush();
//                    if (BuildConfig.DEBUG) {
//                        Log.d(TAG, "HTTP cache flushed");
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "flush - " + e);
//                }
//            }
//        }
    }

    @Override
    protected void closeCacheInternal() {
//        super.closeCacheInternal();
//        synchronized (mHttpDiskCacheLock) {
//            if (mHttpDiskCache != null) {
//                try {
//                    if (!mHttpDiskCache.isClosed()) {
//                        mHttpDiskCache.close();
//                        mHttpDiskCache = null;
//                        if (BuildConfig.DEBUG) {
//                            Log.d(TAG, "HTTP cache closed");
//                        }
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "closeCacheInternal - " + e);
//                }
//            }
//        }
    }

    /**
    * Simple network connection check.
    *
    * @param context
    */
    private void checkConnection(Context context) {
    	
//        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
    	//TODO!!!
//        if (!ServerChecker.checkIsNetworkAvailable(context))
//            Toast.makeText(context, context.getString(R.string.no_network_connection_toast), Toast.LENGTH_LONG).show();
//            //Toast.makeText(context, R.string.no_network_connection_toast, Toast.LENGTH_LONG).show();
//            Log.e(TAG, "checkConnection - no connection found");
//        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String sUrlOrUri) {

        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
		try {

			// check if its URL but not a Local File
			URL url = null;
			try {
				url = new URL(sUrlOrUri);
			} catch (MalformedURLException ignored) {}

			File imageFile = null;
			// if data isn't URL than file's path usually starts with /mnt/
			if (url == null || sUrlOrUri.substring(0, 5).equals("/mnt/")) {
				imageFile = new File(sUrlOrUri);
				// from file
				if (imageFile.exists() /*&& imageFile.canRead()*/) {
					fileInputStream = new FileInputStream(imageFile);
					fileDescriptor = fileInputStream.getFD();
				} 
				// from drawable by Uri
				else 
					inputStream = mContentResolver.openInputStream(Uri.parse(sUrlOrUri));
			} 
			// if data is valid URL check if its already DL-ed
			else {
				imageFile = SDCardHelper.getFileForImageCaching(sUrlOrUri);
				if ((!imageFile.exists() || imageFile.length() == 0) && downloadUrlToFile(url, imageFile)){
					fileInputStream = new FileInputStream(imageFile.toString());
					fileDescriptor = fileInputStream.getFD();
				}
				else { // could be incompletely DL-ed in theory
					fileInputStream = new FileInputStream(imageFile);
					fileDescriptor = fileInputStream.getFD();
				}
			}

		} catch (IOException e) {
			Log.e(TAG, "processBitmap - " + e);
		} catch (IllegalStateException e) {
			Log.e(TAG, "processBitmap - " + e);
		}

        Bitmap bitmap = null;
        
        if (fileDescriptor != null) {
        	//bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth, mImageHeight, getImageCache());
        } 
        else if(fileInputStream!=null)
        	//bitmap = BitmapFactory.decodeStream(inputStream);
        	bitmap = decodeSampledBitmapFromStream(fileInputStream, mImageWidth, mImageHeight, getImageCache());
        else if(inputStream!=null)
        	//bitmap = BitmapFactory.decodeStream(inputStream);
        	bitmap = decodeSampledBitmapFromStream(inputStream, mImageWidth, mImageHeight, getImageCache());
        
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {}
        }
        if (inputStream != null) {
            try {
            	inputStream.close();
            } catch (IOException e) {}
        }
        return bitmap;
    }

    private Bitmap processBitmap(Uri data) {

        InputStream inputStream = null;
		try {
			inputStream = mContentResolver.openInputStream(data);// new FileInputStream(imageFile);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "processBitmap - " + e);
		} catch (IllegalStateException e) {
			Log.e(TAG, "processBitmap - " + e);
		}

        Bitmap bitmap = null;

        if(inputStream!=null)
        	//bitmap = BitmapFactory.decodeStream(inputStream);
        	bitmap = decodeSampledBitmapFromStream(inputStream, mImageWidth, mImageHeight, getImageCache());

        if (inputStream != null) {
            try {
            	inputStream.close();
            } catch (IOException e) {}
        }
        
//        Log.w(this,"processBitmap(Uri): Uri: "+data+" mImageWidth: "+mImageWidth+" mImageHeight: "+mImageHeight+" returning bitmap: "+bitmap);
        
        return bitmap;
    }

    private Bitmap processBitmap(ImageTask imageTask) {
    	
//		Log.i(this,"started processBitmap(ImageTask) to getBitmap");
		return imageTask.getBitmap(this);
//        Bitmap bitmap = null;
//    	if (imageTask.imageResId != 0){
//    		if (!imageTask.makeImageByCrop && imageTask.imageTargetHeight>0 && imageTask.imageTargetWidth>0)
//    			bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageTask.imageResId, Math.min(imageTask.imageTargetHeight, imageTask.imageTargetWidth)/2*3);
//    		else if (!imageTask.makeImageByCrop && imageTask.imageTargetMaxSideSize>0)
//    			bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageTask.imageResId, imageTask.imageTargetMaxSideSize);
//    		else if(!imageTask.makeImageByCrop){ //imageTask.imageTargetHeight==0 && imageTask.imageTargetWidth==0 && imageTask.imageTargetMaxSideSize==0
//    			// getMaxSideSize from imageFetcher
//    			int[] imageSize = getImageSize();
//    			int imageTargetMaxSideSize=0;
//    			if (imageSize[0] == imageSize[1])
//    				imageTargetMaxSideSize = imageSize[0];
//    			else
//    				imageTargetMaxSideSize = Math.max(imageSize[0], imageSize[1]);
//    			bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageTask.imageResId, imageTargetMaxSideSize);
//    		} 
//    		else if (imageTask.makeImageByCrop){
////    			Log.i(this,"started processBitmap(ImageTask) to getBitmap: makeImageByCrop for imageTask.imageResId: "+imageTask.imageResId);
//				bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(mResources, 
//																		 mContentResolver,
//																		 imageTask.imageResId,
//																		 imageTask.imageTargetHeight, 
//																		 imageTask.imageTargetWidth);
////    			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
//    		}
//    	}
//    	// working with url
//    	else if ( !TextUtils.isEmpty(imageTask.imageUrl) ) {
//			// check if its URL but not a Local File
//			URL url = null;
//			try {
//				url = new URL(imageTask.imageUrl);
//			} catch (MalformedURLException e) {
//				imageTask.sendOnErrorEvent(e.getMessage());
//				return null;
//			}
//			
//			File imageFile = null;
//	        FileDescriptor fileDescriptor = null;
//	        FileInputStream fileInputStream = null;
//
//	        if (imageTask.cacheImageOnSD)
//	        	imageFile = SDCardHelper.getFileForImageCaching(imageTask.imageUrl);
//	        else
//	        	imageFile = SDCardHelper.getTempFile();
//	        
//			try{
//				if ((!imageFile.exists() || imageFile.length() == 0) && downloadUrlToFile(url, imageFile)){
//					Log.w(this,"ImageTask: DLing image from HTTP. URL: "+url+" to file: "+imageFile);
//					fileInputStream = new FileInputStream(imageFile.toString());
//					fileDescriptor = fileInputStream.getFD();
//				}
//				else { // could be incompletely DL-ed in theory
//					Log.w(this,"ImageTask: Getting image from SD. URL: "+url+" file: "+imageFile);
//					fileInputStream = new FileInputStream(imageFile);
//					fileDescriptor = fileInputStream.getFD();
//				}
//				
//		        if (fileDescriptor != null) {
//		        	//bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//		            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth, mImageHeight, getImageCache());
//		        } 
//		        else if(fileInputStream!=null)
//		        	//bitmap = BitmapFactory.decodeStream(inputStream);
//		        	bitmap = decodeSampledBitmapFromStream(fileInputStream, mImageWidth, mImageHeight, getImageCache());
//		        
//			} catch (IOException e) {
//				Log.e(TAG, "processBitmap - " + e);
//			} catch (IllegalStateException e) {
//				Log.e(TAG, "processBitmap - " + e);
//			} finally {
//		        if (fileInputStream != null) {
//		            try {
//		                fileInputStream.close();
//		            } catch (IOException ignored) {}
//		        }
//			}
//			
//			bitmap = imageTask.getProcessedBitmap(bitmap, imageFile);
////    		if (imageTask.makeImageByCrop){
//////    			Log.i(this,"started processBitmap(ImageTask) to getBitmap: makeImageByCrop imageTask.imageResId==0");
////				bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(bitmap, imageTask.imageTargetHeight, imageTask.imageTargetWidth);
//////    			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
////    		}
////    		
////			if (imageTask.makeImagePreview){
////				//TODO ImageUtils.makePreviewAndSave(imageFile, previewImageFile, imageTask.imageTargetMaxSideSize);
////			}
////    		
////    		if (!imageTask.cacheImageOnSD){
////    			imageFile.delete();
////    		}
//
//    	}
//    	else if ( imageTask.imageFile!=null && imageTask.imageFile.exists() &&  imageTask.imageFile.canRead() ){
//    		bitmap = imageTask.getProcessedBitmap(bitmap);
////    		if (imageTask.makeImageByCrop && imageTask.imageFile!=null){
//////    			Log.i(this,"started processBitmap(ImageTask) to getBitmap: makeImageByCrop");
////				bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(imageTask.imageFile, 
////																		 imageTask.imageTargetHeight, 
////																		 imageTask.imageTargetWidth);
//////    			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
////    		}
//    	}
//    	else {
//    		Log.e(this, "ImageTask cant be processed!!!");
//    	}
//    	
//    	//imageTask.sendOnCompletionEvent();
//        return bitmap;
    }
    
    @Override
    protected Bitmap processBitmap(Object data) {
    	/* костыль на
   	 	0java.lang.RuntimeException: An error occured while executing doInBackground()
   		01at android.image.utils.ImageAsyncTask$3.done(ImageAsyncTask.java:328)
   		02at java.util.concurrent.FutureTask$Sync.innerSetException(FutureTask.java:273)
  		03at java.util.concurrent.FutureTask.setException(FutureTask.java:124)
  		04at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:307)
  		05at java.util.concurrent.FutureTask.run(FutureTask.java:137)
  		06at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1076)
  		07at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:569)
  		08at java.lang.Thread.run(Thread.java:856)
  		9Caused by: java.lang.IllegalArgumentException: Problem decoding into existing bitmap
  		10at android.graphics.BitmapFactory.decodeFileDescriptor(BitmapFactory.java:743)
  		11at android.image.utils.ImageResizer.decodeSampledBitmapFromDescriptor(ImageResizer.java:195)
  		12at android.image.utils.ImageFetcher.processBitmap(ImageFetcher.java:247)
  		13at android.image.utils.ImageFetcher.processBitmap(ImageFetcher.java:259)
  		14at android.image.utils.ImageWorker$BitmapWorkerTask.doInBackground(ImageWorker.java:274)
  		15at android.image.utils.ImageWorker$BitmapWorkerTask.doInBackground(ImageWorker.java:1)
  		16at android.image.utils.ImageAsyncTask$2.call(ImageAsyncTask.java:316)
  		17at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:305)
  		и заодно на возможный Out of memory
    	 */
		Bitmap bitmap = null;
		try {
			if (data instanceof String){
				bitmap = processBitmap(String.valueOf(data));
//				Log.i(this,"got image by processBitmap(String) h: "+bitmap.getHeight()+" w: "+bitmap.getWidth());
			}
			else if (data instanceof Uri){
				bitmap = processBitmap( (Uri)data );
//				Log.i(this,"got image by processBitmap(Uri) to getBitmap h: "+bitmap.getHeight()+" w: "+bitmap.getWidth());
			}
			else if (data instanceof Integer){
				bitmap = super.processBitmap( ((Integer)data).intValue() );
//				Log.i(this,"got image by processBitmap(Integer) to getBitmap h: "+bitmap.getHeight()+" w: "+bitmap.getWidth());
			}
			else if (data instanceof ImageTask){
				bitmap = processBitmap( (ImageTask)data );
//				Log.i(this,"got image by processBitmap(ImageTask) to getBitmap h: "+bitmap.getHeight()+" w: "+bitmap.getWidth());
			}
		} catch (Exception e) {
			// e.printStackTrace();
		} catch (Throwable e) {
			// e.printStackTrace();
		}
		return bitmap;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
	public static final int TIME_OUT=10000;
    public boolean downloadUrlToFile(final URL imageUrl, final File imageFile) {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        
		try {
        	final File fullsizedImageFile = SDCardHelper.getFileForImageCaching(imageUrl);
        	urlConnection = (HttpURLConnection)imageUrl.openConnection();
        	urlConnection.setConnectTimeout(TIME_OUT);
        	urlConnection.setReadTimeout(TIME_OUT);
        	urlConnection.setInstanceFollowRedirects(true);
            inputStream = urlConnection.getInputStream();
            
            // льем поток прямо в файл
            FileUtils.streamToFile(inputStream, fullsizedImageFile);
            
            final File previewImageFile = SDCardHelper.getFileForPreviewImageCaching(imageUrl);
            ImageUtils.makePreviewAndSave(fullsizedImageFile, previewImageFile, DeviceInfo.getDeviceMaxSideSizeByDensity());
            	
            //Bitmap bm = ImageUtils.getBitmapFromFile(fullsizedImageFile);
            processBitmap(imageUrl.toString());
            return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
        } finally {
        	
          if (urlConnection != null)
              urlConnection.disconnect();

          if (inputStream != null)
              try {
            	  inputStream.close();
              } catch (final IOException e) {}
        }
		
//        HttpURLConnection urlConnection = null;
//        BufferedOutputStream out = null;
//        BufferedInputStream in = null;
//
//        try {
//            //final URL url = new URL(urlString);
//            urlConnection = (HttpURLConnection) url.openConnection();
//            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
//            //out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
//            //final File imageFile = SDCardHelper.getFileForImageCaching(urlString);
//            Log.i(TAG,"downloadUrlToStream: "+imageFile.toString());
//            out = new BufferedOutputStream(new FileOutputStream(imageFile), IO_BUFFER_SIZE);
//
//            int b;
//            while ((b = in.read()) != -1) {
//                out.write(b);
//            }
//            return true;
//        } catch (final IOException e) {
//            Log.e(TAG, "Error in downloadBitmap - " + e);
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            try {
//                if (out != null) {
//                    out.close();
//                }
//                if (in != null) {
//                    in.close();
//                }
//            } catch (final IOException e) {}
//        }
        return false;
    }

   
    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

}

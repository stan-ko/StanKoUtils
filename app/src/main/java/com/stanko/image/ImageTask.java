package com.stanko.image;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.stanko.tools.DeviceInfo;
import com.stanko.tools.FileUtils;
import com.stanko.tools.Log;
import com.stanko.tools.SDCardHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageTask {

    //	public View targetImageView;
    public final String		imageUrl;
    public final Uri		imageUri;
    public final File		imageFile;
    public final int		imageResId;
    public final int		imageTargetWidth;
    public final int		imageTargetHeight;
    public final int		imageTargetMaxSideSize;
    public final boolean	makeImageByCrop;
    public final boolean	makeImageByLimitSideSize;
    public final boolean	makeImagePreview;
    public final boolean	cacheImageOnSD;
    public final boolean	isOptimistic=true;

    public ImageTask (String url){
        this(url,0);
    }

    public ImageTask (String url, int maxPreviewSideSize){
        this(url,false,maxPreviewSideSize);
    }

    public ImageTask (String url, boolean makePreview, int maxPreviewSideSize){
        // resource
        this.imageUrl = url;
        this.imageUri = null;
        this.imageFile = null;
        this.imageResId = 0;
        // limit side size
        this.imageTargetMaxSideSize = maxPreviewSideSize;
        this.makeImageByLimitSideSize = imageTargetMaxSideSize>0;
        // scaling/cropping
        this.imageTargetWidth = 0;
        this.imageTargetHeight = 0;
        // cropping
        this.makeImageByCrop = false;
        // make preview
        this.makeImagePreview = makePreview;
        // cache
        this.cacheImageOnSD = true;
    }

    public ImageTask (String url, boolean makePreview, int maxTargetSideSize, int imageTargetHeight, int imageTargetWidth, boolean makeImageByCrop){
//		this(url, cacheOnSD, false, imageTargetHeight, imageTargetWidth, makeImageByCrop);
//	}
//	public ImageTask (String url, boolean cacheOnSD, boolean makePreview, int imageTargetHeight, int imageTargetWidth, boolean makeImageByCrop){
        // resource
        this.imageUrl = url;
        this.imageUri = null;
        this.imageFile = null;
        this.imageResId = 0;

        // limit side size
        this.imageTargetMaxSideSize = 0; //DeviceInfo.getSmallestScreenSideSize();
        this.makeImageByLimitSideSize = imageTargetMaxSideSize>0;

        // scaling/cropping
        this.imageTargetWidth = imageTargetWidth;
        this.imageTargetHeight = imageTargetHeight;

        // cropping
        this.makeImageByCrop = makeImageByCrop;

        // make preview
        this.makeImagePreview = makePreview;
        // cache
        this.cacheImageOnSD = true;
    }

    public ImageTask (String url, int imageTargetHeight, int imageTargetWidth, boolean makeImageByCrop){
        // resource
        this.imageUrl = url;
        this.imageUri = null;
        this.imageFile = null;
        this.imageResId = 0;

        // limit side size
        this.imageTargetMaxSideSize = 0; //DeviceInfo.getSmallestScreenSideSize();
        this.makeImageByLimitSideSize = true;

        // scaling/cropping
        this.imageTargetWidth = imageTargetWidth;
        this.imageTargetHeight = imageTargetHeight;

        // cropping
        this.makeImageByCrop = makeImageByCrop;

        // make preview
        this.makeImagePreview = false;
        // cache
        this.cacheImageOnSD = true;
    }



    public ImageTask (File file){
        this(file,0);
    }

    public ImageTask (File file, int maxPreviewSideSize){
        this(file, false, maxPreviewSideSize);
    }

    public ImageTask (File file, boolean makePreview, int maxPreviewSideSize){
        // resource
        this.imageUrl = null;
        this.imageUri = Uri.fromFile(file);
        this.imageFile = file;
        this.imageResId = 0;
        // limit side size
        this.imageTargetMaxSideSize = maxPreviewSideSize;
        this.makeImageByLimitSideSize = imageTargetMaxSideSize>0;
        // scaling/cropping
        this.imageTargetWidth = 0;
        this.imageTargetHeight = 0;
        // cropping
        this.makeImageByCrop = false;
        // make preview
        this.makeImagePreview = makePreview;
        // cache
        this.cacheImageOnSD = false;
    }

    public ImageTask (File file, boolean makePreview, int maxTargetSideSize, int imageTargetHeight, int imageTargetWidth, boolean makeImageByCrop){

        this.imageUrl = null;
        this.imageUri = Uri.fromFile(file);
        this.imageFile = file;
        this.imageResId = 0;

        // limit side size
        this.imageTargetMaxSideSize = 0; //DeviceInfo.getSmallestScreenSideSize();
        this.makeImageByLimitSideSize = imageTargetMaxSideSize>0;

        // scaling/cropping
        this.imageTargetWidth = imageTargetWidth;
        this.imageTargetHeight = imageTargetHeight;

        // cropping
        this.makeImageByCrop = makeImageByCrop;

        // make preview
        this.makeImagePreview = makePreview;
        // cache
        this.cacheImageOnSD = false;
    }

    public ImageTask (File file, int imageTargetHeight, int imageTargetWidth, boolean makeImageByCrop){
        // resource
        this.imageUrl = null;
        this.imageUri = Uri.fromFile(file);
        this.imageFile = file;
        this.imageResId = 0;

        // limit side size
        this.imageTargetMaxSideSize = 0; //DeviceInfo.getSmallestScreenSideSize();
        this.makeImageByLimitSideSize = true;

        // scaling/cropping
        this.imageTargetWidth = imageTargetWidth;
        this.imageTargetHeight = imageTargetHeight;

        // cropping
        this.makeImageByCrop = makeImageByCrop;

        // make preview
        this.makeImagePreview = false;
        // cache
        this.cacheImageOnSD = false;
    }



    public ImageTask (int imgeResId){
        this(imgeResId,0);
    }

    public ImageTask (int imgeResId, int maxTargetSideSize){
        this.imageUrl = null;
        this.imageUri = null;
        this.imageFile = null;
        this.imageResId = imgeResId;
        this.imageTargetMaxSideSize = maxTargetSideSize;
        this.imageTargetWidth = 0;
        this.imageTargetHeight = 0;
        this.makeImageByCrop = false;
        this.makeImageByLimitSideSize = true;
        this.makeImagePreview = false;
        this.cacheImageOnSD = false;
    }

    public ImageTask (int imgeResId, int imageTargetHeight, int imageTargetWidth, boolean makeImageByCrop){
        this.imageUrl = null;
        this.imageUri = null;
        this.imageFile = null;
        this.imageResId = imgeResId;
        this.imageTargetMaxSideSize = 0; //DeviceInfo.getSmallestScreenSideSize();
        this.imageTargetWidth = imageTargetWidth;
        this.imageTargetHeight = imageTargetHeight;
        this.makeImageByCrop = makeImageByCrop;
        this.makeImageByLimitSideSize = true;
        this.makeImagePreview = false;
        this.cacheImageOnSD = false;
    }

    @Override
    public String toString() {
        String toStringKeyToDetectImageInCache = "imgsrc:";
        if (imageUrl!=null)
            toStringKeyToDetectImageInCache += imageUrl;
        else if (imageFile!=null){ // same name but rewritten file, using length + lastModified
            toStringKeyToDetectImageInCache += imageFile.toString() + String.valueOf(imageFile.lastModified()) + String.valueOf(imageFile.length());
        }
        else
            toStringKeyToDetectImageInCache += String.valueOf(imageResId);

        toStringKeyToDetectImageInCache+=
                "\nMaxSideSize: "+ String.valueOf(imageTargetMaxSideSize)
                        + " TargetWidth: "+ String.valueOf(imageTargetWidth)
                        + " TargetHeight:"+ String.valueOf(imageTargetHeight)
                        + " makeImagePreview: "+ String.valueOf(makeImagePreview)
                        + " makeImageByCrop: "+ String.valueOf(makeImageByCrop)
                        + " makeImageByLimitSideSize: " + String.valueOf(makeImageByLimitSideSize)
                        + " cacheImageOnSD: " + String.valueOf(cacheImageOnSD) ;

        return toStringKeyToDetectImageInCache;
    }

    private ImageTaskListener imageTaskListener;
    public interface ImageTaskListener{
        public void onError(String errMsg);
        public void onOnCompletion();
    }
    public void addImageTaskListener (ImageTaskListener imageTaskListener){
        this.imageTaskListener = imageTaskListener;
    }
    public void setImageTaskListener (ImageTaskListener imageTaskListener){
        this.imageTaskListener = imageTaskListener;
    }
    public void removeImageTaskListener (){
        this.imageTaskListener = null;
    }
    public void sendOnErrorEvent(String errMsg){
        //Log.w(this, ">>>>>>>>>>>>>>>>>>>>>>>>>>>> onOnError: "+errMsg);
        if (imageTaskListener!=null)
            imageTaskListener.onError(errMsg);
        removeImageTaskListener();
    }
    public void sendOnCompletionEvent(){
//		Log.w(this, ">>>>>>>>>>>>>>>>>>>>>>>>>>>> onOnCompletion");
        if (imageTaskListener!=null)
            imageTaskListener.onOnCompletion();
        removeImageTaskListener();
    }


    public Bitmap getBitmap(ImageFetcher mImageFetcher){

        Bitmap bitmap = null;
        Resources mResources = mImageFetcher.mResources;
        ContentResolver mContentResolver = mImageFetcher.mContentResolver;

        //Log.i(this,"getBitmap(): "+this);
        //imageTargetMaxSideSize: "+imageTargetMaxSideSize+" imageTargetHeight: "+imageTargetHeight+" imageTargetWidth: "+imageTargetWidth);

        if (imageResId != 0){
//    		if (!makeImageByCrop && imageTargetHeight>0 && imageTargetWidth>0)
//    			bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageResId, Math.min(imageTargetHeight, imageTargetWidth)/2*3);
//    		else if (!makeImageByCrop && imageTargetMaxSideSize>0)
//    			bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageResId, imageTargetMaxSideSize);

//    		if(!makeImageByCrop){ //imageTargetHeight==0 && imageTargetWidth==0 && imageTargetMaxSideSize==0
//    			int targetMaxSideSize=0;
//    			if (makeImageByLimitSideSize || imageTargetMaxSideSize==0 && imageTargetWidth==0 && imageTargetHeight==0){
//        			// getMaxSideSize from imageFetcher
//	    			int[] imageSize = mImageFetcher.getImageSize();
//	    			if (imageSize[0] == imageSize[1])
//	    				targetMaxSideSize = imageSize[0];
//	    			else
//	    				targetMaxSideSize = Math.max(imageSize[0], imageSize[1]);
//    			}
//    			else {
//    				if (imageTargetMaxSideSize>0)
//    					targetMaxSideSize = imageTargetMaxSideSize;
//    				else
//    					targetMaxSideSize = Math.max(imageTargetWidth, imageTargetHeight);
//    			}
//    			bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageResId, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
//    		} 
            if(!makeImageByCrop){ //imageTargetHeight==0 && imageTargetWidth==0 && imageTargetMaxSideSize==0
                int targetMaxSideSize=0;
                // никакие целевые размеры не заданы вообще, берем ограничение из fetcher-a
                if (makeImageByLimitSideSize || imageTargetMaxSideSize==0 && imageTargetWidth==0 && imageTargetHeight==0){
                    // getMaxSideSize from imageFetcher
                    int[] imageSize = mImageFetcher.getImageSize();
                    if (imageSize[0] == imageSize[1])
                        targetMaxSideSize = imageSize[0];
                    else
                        targetMaxSideSize = Math.max(imageSize[0], imageSize[1]);
                }
                // был задан или взят у fetcher-а максимальный размер стороны
                if (imageTargetMaxSideSize>0 || targetMaxSideSize>0) {
                    if (imageTargetMaxSideSize>0) // был задан целевой размер стороны
                        targetMaxSideSize = imageTargetMaxSideSize;
                    bitmap = ImageUtils.getBitmapFromResources(mResources, mContentResolver, imageResId, imageTargetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    //bitmap = ImageUtils.getBitmapFromFile(workingImageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    // если с ограничением по стороне были заданы целевые и высота и ширина
                    if (imageTargetWidth>0 && imageTargetHeight>0)
                        bitmap = ImageUtils.getResizedBitmap(bitmap, imageTargetHeight, imageTargetWidth);
                }
                else{ // были заданы целевые размеры минимум по одной стороне
                    //targetMaxSideSize = Math.max(imageTargetWidth, imageTargetHeight);
                    // была задана целевая высота
                    if (imageTargetWidth==0 && imageTargetHeight>0){
                        bitmap = ImageUtils.getBitmapFromResourcesWithMaxHeight(mResources, mContentResolver, imageResId, imageTargetHeight, isOptimistic); //TODO: isOptimistic
                    }
                    // была задана целевая ширина
                    else if (imageTargetWidth>0 && imageTargetHeight==0){
                        bitmap = ImageUtils.getBitmapFromResourcesWithMaxWidth(mResources, mContentResolver, imageResId, imageTargetWidth, isOptimistic); //TODO: isOptimistic
                    }
                    // были заданы целевые и высота и ширина
                    else {
                        bitmap = ImageUtils.getResizedBitmapFromResources(mResources, mContentResolver, imageResId, imageTargetHeight, imageTargetWidth, isOptimistic); //TODO: isOptimistic
                        //bitmap = ImageUtils.BitmapFromFile(imageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    }
                }
            }
            else if (makeImageByCrop){
                bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(mResources,
                        mContentResolver,
                        imageResId,
                        imageTargetHeight,
                        imageTargetWidth);
//    			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
            }

//            if (makeImagePreview){
//	            final File previewImageFile = SDCardHelper.getFileForPreviewImageCaching(imageUrl);
//	            ImageUtils.makePreviewAndSave(bitmap, previewImageFile, DeviceInfo.getDeviceMaxSideSizeByDensity());
//            }

        }
        // working with url
        else if ( !TextUtils.isEmpty(imageUrl) ) {
            // check if its URL but not a Local File
            URL url = null;
            try {
                url = new URL(imageUrl);
            } catch (MalformedURLException e) {
//				sendOnErrorEvent(e.getMessage());
                return null;
            }

            File workingImageFile = null;

            if (cacheImageOnSD)
                workingImageFile = SDCardHelper.getFileForImageCaching(imageUrl);
            else
                workingImageFile = SDCardHelper.getTempFile();

            if ( !workingImageFile.exists() || workingImageFile.length() == 0 ){
//				Log.w(this,"getBitmap() by URL: DLing image from HTTP. URL: "+url+" to file: "+workingImageFile);
                if ( !downloadUrlToFile(url, workingImageFile) )
                    workingImageFile = null; // failed to DL image
            }
            else { // could be incompletely DL-ed in theory
//				Log.w(this,"getBitmap() by URL: Getting image from SD. URL: "+url+" file: "+workingImageFile);
            }

            // failed to DL image
            if (workingImageFile == null){
//				Log.w(this,"getBitmap() by URL: workingImageFile == null -> Error DLing image!");
                return null;
            }

//    		if(!makeImageByCrop){ //imageTargetHeight==0 && imageTargetWidth==0 && imageTargetMaxSideSize==0
//    			int targetMaxSideSize=0;
//    			if (makeImageByLimitSideSize || imageTargetMaxSideSize==0 && imageTargetWidth==0 && imageTargetHeight==0){
//        			// getMaxSideSize from imageFetcher
//	    			int[] imageSize = mImageFetcher.getImageSize();
//	    			if (imageSize[0] == imageSize[1])
//	    				targetMaxSideSize = imageSize[0];
//	    			else
//	    				targetMaxSideSize = Math.max(imageSize[0], imageSize[1]);
//    			}
//    			else {
//    				if (imageTargetMaxSideSize>0)
//    					targetMaxSideSize = imageTargetMaxSideSize;
//    				else
//    					targetMaxSideSize = Math.max(imageTargetWidth, imageTargetHeight);
//    			}
////				Log.w(this,"getBitmap() making not cropped image");
//    			bitmap = ImageUtils.getBitmapFromFile(workingImageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
//    		}
            if(!makeImageByCrop){ //imageTargetHeight==0 && imageTargetWidth==0 && imageTargetMaxSideSize==0
                int targetMaxSideSize=0;
                // никакие целевые размеры не заданы вообще, берем ограничение из fetcher-a
                if (makeImageByLimitSideSize || imageTargetMaxSideSize==0 && imageTargetWidth==0 && imageTargetHeight==0){
                    // getMaxSideSize from imageFetcher
                    int[] imageSize = mImageFetcher.getImageSize();
                    if (imageSize[0] == imageSize[1])
                        targetMaxSideSize = imageSize[0];
                    else
                        targetMaxSideSize = Math.max(imageSize[0], imageSize[1]);
                }
                // был задан или взят у fetcher-а максимальный размер стороны
                if (imageTargetMaxSideSize>0 || targetMaxSideSize>0) {
                    if (imageTargetMaxSideSize>0) // был задан целевой размер стороны
                        targetMaxSideSize = imageTargetMaxSideSize;
                    bitmap = ImageUtils.getBitmapFromFile(workingImageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    // если с ограничением по стороне были заданы целевые и высота и ширина
                    if (imageTargetWidth>0 && imageTargetHeight>0)
                        bitmap = ImageUtils.getResizedBitmap(bitmap, imageTargetHeight, imageTargetWidth);
                }
                else{ // были заданы целевые размеры минимум по одной стороне
                    //targetMaxSideSize = Math.max(imageTargetWidth, imageTargetHeight);
                    // была задана целевая высота
                    if (imageTargetWidth==0 && imageTargetHeight>0){
                        bitmap = ImageUtils.getBitmapFromFileWithMaxHeight(workingImageFile, imageTargetHeight, isOptimistic); //TODO: isOptimistic
                    }
                    // была задана целевая ширина
                    else if (imageTargetWidth>0 && imageTargetHeight==0){
                        bitmap = ImageUtils.getBitmapFromFileWithMaxWidth(workingImageFile, imageTargetWidth, isOptimistic); //TODO: isOptimistic
                    }
                    // были заданы целевые и высота и ширина
                    else {
                        bitmap = ImageUtils.getResizedBitmapFromFile(workingImageFile, imageTargetHeight, imageTargetWidth, isOptimistic); //TODO: isOptimistic
                        //bitmap = ImageUtils.BitmapFromFile(imageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    }
                }
            }
            else if (makeImageByCrop){
//				Log.w(this,"getBitmap() making cropped image");
                bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(workingImageFile,
                        imageTargetHeight,
                        imageTargetWidth);
//    			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
            }

            if (makeImagePreview){
//				Log.w(this,"getBitmap() making preview image");
                final File previewImageFile = SDCardHelper.getFileForPreviewImageCaching(imageUrl);
                ImageUtils.makePreviewAndSave(workingImageFile, previewImageFile, DeviceInfo.getDeviceMaxSideSizeByDensity());
            }

//    		if (!cacheImageOnSD)
//    			workingImageFile.delete();

        }
        // by the file
        else if ( FileUtils.isReadable(imageFile) ){

            if(!makeImageByCrop){ //imageTargetHeight==0 && imageTargetWidth==0 && imageTargetMaxSideSize==0
                int targetMaxSideSize=0;
                // никакие целевые размеры не заданы вообще, берем ограничение из fetcher-a
                if (makeImageByLimitSideSize || imageTargetMaxSideSize==0 && imageTargetWidth==0 && imageTargetHeight==0){
                    // getMaxSideSize from imageFetcher
                    int[] imageSize = mImageFetcher.getImageSize();
                    if (imageSize[0] == imageSize[1])
                        targetMaxSideSize = imageSize[0];
                    else
                        targetMaxSideSize = Math.max(imageSize[0], imageSize[1]);
                }
                // был задан или взят у fetcher-а максимальный размер стороны
                if (imageTargetMaxSideSize>0 || targetMaxSideSize>0) {
                    if (imageTargetMaxSideSize>0) // был задан целевой размер стороны
                        targetMaxSideSize = imageTargetMaxSideSize;
                    bitmap = ImageUtils.getBitmapFromFile(imageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    // если с ограничением по стороне были заданы целевые и высота и ширина
                    if (imageTargetWidth>0 && imageTargetHeight>0)
                        bitmap = ImageUtils.getResizedBitmap(bitmap, imageTargetHeight, imageTargetWidth);
                }
                else{ // были заданы целевые размеры минимум по одной стороне
                    //targetMaxSideSize = Math.max(imageTargetWidth, imageTargetHeight);
                    // была задана целевая высота
                    if (imageTargetWidth==0 && imageTargetHeight>0){
                        bitmap = ImageUtils.getBitmapFromFileWithMaxHeight(imageFile, imageTargetHeight, isOptimistic); //TODO: isOptimistic
                    }
                    // была задана целевая ширина
                    else if (imageTargetWidth>0 && imageTargetHeight==0){
                        bitmap = ImageUtils.getBitmapFromFileWithMaxWidth(imageFile, imageTargetWidth, isOptimistic); //TODO: isOptimistic
                    }
                    // были заданы целевые и высота и ширина
                    else {
                        bitmap = ImageUtils.getResizedBitmapFromFile(imageFile, imageTargetHeight, imageTargetWidth, isOptimistic); //TODO: isOptimistic
                        //bitmap = ImageUtils.BitmapFromFile(imageFile, targetMaxSideSize, isOptimistic); //TODO: isOptimistic
                    }
                }
            }
            else if (makeImageByCrop){
                bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(imageFile,
                        imageTargetHeight,
                        imageTargetWidth);
//    			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
            }

            if (makeImagePreview){
                final File previewImageFile = SDCardHelper.getFileForPreviewImageCaching(imageUrl);
                ImageUtils.makePreviewAndSave(imageFile, previewImageFile, DeviceInfo.getDeviceMaxSideSizeByDensity());
            }

        }
        else {
            Log.e(this, "ImageTask can't be processed - unknown source:\nUrl: "+ imageUrl+"\nUri: "+imageUri+"\nFile: "+imageFile+"\nResId: "+imageResId);
        }

        //sendOnCompletionEvent();
        return bitmap;
    }

//	public BitmapDrawable getProcessedRecyclingBitmap(Resources resources, Bitmap bmp, File...workFile){
//		RecyclingBitmapDrawable bitmap = null;
//		if (makeImageByCrop){
////			Log.i(this,"started processBitmap(ImageTask) to getBitmap: makeImageByCrop imageResId==0");
//			bitmap = new RecyclingBitmapDrawable (resources, ImageUtils.getBitmapByCropFromCenterWithScaling(bmp, imageTargetHeight, imageTargetWidth) );
////			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
//		}
//		
//		if (makeImagePreview){
//			//TODO ImageUtils.makePreviewAndSave(imageFile, previewImageFile, imageTargetMaxSideSize);
//		}
//		
//		if (!cacheImageOnSD && workFile.length>0 && workFile[0].exists()){
//			workFile[0].delete();
//		}
//		
//		return bitmap;
//	}
//
//	public Bitmap getProcessedBitmap(Bitmap bmp, File...workFile){
//		Bitmap bitmap = null;
//		//if ( imageFile!=null && imageFile.exists() &&  imageFile.canRead() ){
//    		if (makeImageByCrop){
//    			bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(bmp, imageTargetHeight, imageTargetWidth);
//    		}
//    		else if (imageTargetMaxSideSize>0)
//    			bitmap = ImageUtils.getBitmapFromFile(imageFile, imageTargetMaxSideSize);
//    		else if (imageTargetHeight>0 || imageTargetWidth==0)
//    			bitmap = ImageUtils.getBitmapFromFileWithMaxHeight(imageFile, imageTargetHeight);
//    		else if (imageTargetHeight==0 || imageTargetWidth>0)
//    			bitmap = ImageUtils.getBitmapFromFileWithMaxWidth(imageFile, imageTargetWidth);
//// TODO   		else if (imageTargetHeight>0 && imageTargetWidth>0)
////    			bitmap = ImageUtils.getResizedBitmap(bitmap, newHeight, newWidth) BitmapFromFileWithMaxWidth(imageFile, imageTargetWidth);
//    		else
//    			bitmap = ImageUtils.getBitmapFromFile(imageFile);
//		//}
//		
//		if (makeImageByCrop){
////			Log.i(this,"started processBitmap(ImageTask) to getBitmap: makeImageByCrop imageResId==0");
//			bitmap = ImageUtils.getBitmapByCropFromCenterWithScaling(bmp, imageTargetHeight, imageTargetWidth);
////			Log.i(this,"processBitmap(ImageTask) to getBitmap: makeImageByCrop result: "+bitmap);
//		}
//		
//		if (makeImagePreview){
//			//TODO ImageUtils.makePreviewAndSave(imageFile, previewImageFile, imageTargetMaxSideSize);
//		}
//		
//		if (!cacheImageOnSD && workFile.length>0 && workFile[0].exists()){
//			workFile[0].delete();
//		}
//		
//		return bitmap;
//	}
//
//	public Drawable getProcessedRecyclingBitmap(Resources resources, BitmapDrawable bmp) {
//		// TODO Auto-generated method stub
//		return bmp;
//	}


    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise 
     */
    private static final int TIME_OUT=777*7;
    private boolean downloadUrlToFile(final URL imageUrl, final File imageFile) {

        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        boolean isSuccessfullyDLed = false;
        try {
            urlConnection = (HttpURLConnection)imageUrl.openConnection();
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(TIME_OUT);
            urlConnection.setInstanceFollowRedirects(true);
            inputStream = urlConnection.getInputStream();

            // льем поток прямо в файл
            FileUtils.streamToFile(inputStream, imageFile);

            isSuccessfullyDLed = true;

//            Log.w(this, "downloadUrlToFile() succeed: "+isSuccessfullyDLed+" imageFile: "+imageFile+" imageFile.size: "+imageFile.length());
        } catch (MalformedURLException e) {
            e.printStackTrace();
//			sendOnErrorEvent(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
//			sendOnErrorEvent(e.getMessage());
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
        return isSuccessfullyDLed;
    }


    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    private static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

}
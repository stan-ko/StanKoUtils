package com.stanko.view;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.stanko.BuildConfig;
import com.stanko.tools.Log;
import com.stanko.tools.SdkBuildUtils;

/**
 * A BitmapDrawable that keeps track of whether it is being displayed or cached.
 * When the drawable is no longer being displayed or cached,
 * {@link Bitmap#recycle() recycle()} will be called on this drawable's bitmap.
 */
public class RecyclingBitmapDrawable extends BitmapDrawable {

    static final String LOG_TAG = "RecyclingBitmapDrawable";

    private int mCacheRefCount = 0;
    private int mDisplayRefCount = 0;
    
    public final int sizeInBytes; //readonly!

    private boolean mHasBeenDisplayed;

    @SuppressLint("NewApi")
	public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
    	
        super(res, bitmap);
        
        if (bitmap==null){
        	sizeInBytes =0;
        	return;
        }
        
        if (SdkBuildUtils.hasHoneycombMR1()) {
        	sizeInBytes = bitmap.getByteCount() / 1024;
        } else {
        	sizeInBytes =  bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    }

    public int getSizeInBytes(){
    	return sizeInBytes;
    }
    
    /**
     * Notify the drawable that the displayed state has changed. Internally a
     * count is kept so that the drawable knows when it is no longer being
     * displayed.
     *
     * @param isDisplayed - Whether the drawable is being displayed or not
     */
    public void setIsDisplayed(boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }

        // Check to see if recycle() can be called
        checkState();
    }

    /**
     * Notify the drawable that the cache state has changed. Internally a count
     * is kept so that the drawable knows when it is no longer being cached.
     *
     * @param isCached - Whether the drawable is being cached or not
     */
    public void setIsCached(boolean isCached) {
        synchronized (this) {
            if (isCached) {
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }

        // Check to see if recycle() can be called
        checkState();
    }

    private synchronized void checkState() {
        // If the drawable cache and display ref counts = 0, and this drawable
        // has been displayed, then recycle
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed && hasValidBitmap()) {
            if (BuildConfig.DEBUG)
                Log.d(LOG_TAG, "No longer being used or cached so recycling. " + toString());
            getBitmap().recycle();
        }
    }

    private synchronized boolean hasValidBitmap() {
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }

} 
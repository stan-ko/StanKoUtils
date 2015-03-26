package com.stanko.tools;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.stanko.image.ImageUtils;
import com.stanko.image.ImageUtils.BitmapInfo;

public class ViewSizeHelper {
	
	private static int lastUsedResId;
	private static BitmapInfo lastUsedBitmapInfo;
	
	public static int fixHeight(View view2Fix, int imageResId, int targetWidth){
		if (view2Fix==null || imageResId==0)
			return 0;
		if (lastUsedResId != imageResId){
			BitmapInfo mBitmapInfo = ImageUtils.getBitmapInfoFromResources(view2Fix.getContext(), imageResId);
			if (mBitmapInfo == null)
				return 0;
			lastUsedResId = imageResId;
			lastUsedBitmapInfo = mBitmapInfo;
		}
		
		final int targetHeight = (int) lastUsedBitmapInfo.getHeightByWidth(targetWidth);
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.height = targetHeight;
		view2Fix.setLayoutParams(viewLP);
		return targetHeight;
	}
	
	public static int fixWidth(View view2Fix, int imageResId, int targetHeight){
		if (view2Fix==null || imageResId==0)
			return 0;
		if (lastUsedResId != imageResId){
			BitmapInfo mBitmapInfo = ImageUtils.getBitmapInfoFromResources(view2Fix.getContext(), imageResId);
			if (mBitmapInfo == null)
				return 0;
			lastUsedResId = imageResId;
			lastUsedBitmapInfo = mBitmapInfo;
		}
		
		final int targetWidth = (int) lastUsedBitmapInfo.getWidthByHeight(targetHeight); 
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.width = targetWidth;
		view2Fix.setLayoutParams(viewLP);
		return targetWidth;
	}

	public static int fixHeight(View view2Fix, Bitmap bitmap, int targetWidth){
		if (view2Fix==null || bitmap==null)
			return 0;
		BitmapInfo mBitmapInfo = new BitmapInfo(bitmap);
		
		final int targetHeight = (int) mBitmapInfo.getHeightByWidth(targetWidth);
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.height = targetHeight;
		view2Fix.setLayoutParams(viewLP);
		return targetHeight;
	}
	
	public static int fixWidth(View view2Fix, Bitmap bitmap, int targetHeight){
		if (view2Fix==null || bitmap==null)
			return 0;
		BitmapInfo mBitmapInfo = new BitmapInfo(bitmap);
		
		final int targetWidth = (int) mBitmapInfo.getWidthByHeight(targetHeight); 
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.width = targetWidth;
		view2Fix.setLayoutParams(viewLP);
		return targetWidth;
	}

	
	public static void setHeightAndWitdh(View view2Fix, int targetHeight, int targetWidth){
		if (view2Fix==null)
			return;
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.height = targetHeight;
		viewLP.width = targetWidth;
		view2Fix.setLayoutParams(viewLP);
	}

	public static void setHeight(View view2Fix, int targetHeight){
		if (view2Fix==null)
			return;
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.height = targetHeight;
		view2Fix.setLayoutParams(viewLP);
	}
	
	public static void setWidth(View view2Fix, int targetWidth){
		if (view2Fix==null)
			return;
		final ViewGroup.LayoutParams viewLP = (ViewGroup.LayoutParams) view2Fix.getLayoutParams();
		viewLP.width = targetWidth;
		view2Fix.setLayoutParams(viewLP);
	}
	
	
	public static void requestViewSize(final View view, final IViewSize callBack){
		view.getViewTreeObserver().addOnGlobalLayoutListener( 
        	    new OnGlobalLayoutListener(){
					@SuppressWarnings("deprecation")
					@Override
        	        public void onGlobalLayout() {
        	            //This method was deprecated in API level 16. Use #removeOnGlobalLayoutListener instead
        	            if(DeviceInfo.hasAPI16())
        	            	view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        	            else
        	            	view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        	            // returning result
        	            callBack.onGotViewSize(view.getHeight(), view.getWidth());
        	        }
        	});
	}
	
	public interface IViewSize{
		public void onGotViewSize(final int height, final int width);
	} 
	
//	public static final class ViewSize {
//		public final int height, width;
//		public ViewSize(int height, int width){
//			this.height = height;
//			this.width = width;
//		}
//	}
}

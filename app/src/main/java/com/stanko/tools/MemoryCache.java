package com.stanko.tools;

import java.util.Iterator;
import java.util.LinkedList;

import android.support.v4.util.LruCache;

import com.stanko.BuildConfig;
import com.stanko.view.RecyclingBitmapDrawable;


public class MemoryCache {
	
	private static MemoryCache memoryCacheInstance;
	
	public static MemoryCache getInstance(){
		if (memoryCacheInstance==null)
			memoryCacheInstance = new MemoryCache();
		return memoryCacheInstance;
	}

    private static final String TAG = "MemoryCache";
    //private LruCache<String, Bitmap> cache=Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10,1.5f,true));//Last argument true for LRU ordering

    private final LruCache<String, RecyclingBitmapDrawable> cache;
    private final LinkedList<String> cacheKeys;
    
    private long allocatedSize=0;//current allocated size
    //private long limit=1000000;//max memory in bytes

    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxAllowedMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/Xth of the available memory for this memory cache.
    final int cacheSize = maxAllowedMemory / 4;
    
    public MemoryCache(){
    	cacheKeys = new LinkedList<String>();
        //use 25% of available heap size
//        setLimit(Runtime.getRuntime().maxAllowedMemory() / 1024/8);
        cache = new LruCache<String, RecyclingBitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, RecyclingBitmapDrawable bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                //return bitmap.getByteCount() / 1024;
            	return  bitmap.sizeInBytes; //  bitmap.getRowBytes() * bitmap.getHeight() /1024;
            }
        };
    }
    
//    public void setLimit(long new_limit){
//    	//maxAllowedMemory = (int)new_limit;
//        Log.i(TAG, "MemoryCache2 will use up to "+maxAllowedMemory+"kB");
//    }

    public RecyclingBitmapDrawable get(String id){
        try{
            if(!cacheKeys.contains(id))
                return null;
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(id);
        }catch(NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
    }

    
//    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
//        if (getBitmapFromMemCache(key) == null) {
//        	cache.put(key, bitmap);
//        }
//    }

//    public Bitmap getBitmapFromMemCache(String key) {
//        return cache.get(key);
//    }
    
    
    public void put(String id, RecyclingBitmapDrawable bmp){
        try{
        	// if same url bitmap comes it could be other bitmap
        	// after being for a long time on background for example
        	if (bmp == null || cacheKeys.contains(id) /*&& bitmap.equals(cache.get(id))*/)
        		return;
        	
//       		allocatedSize-=getSizeInKBytes(cache.get(id));
//       		cache.remove(id);
//       		cacheKeys.remove(id);
       		cache.put(id, bmp);
        	bmp.setIsCached(true);
       		
       		cacheKeys.add(id);

            //allocatedSize+=getSizeInKBytes(bmp);
       		allocatedSize+=bmp.sizeInBytes/1024;
            checkSize();
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    
    private void checkSize() {
        Log.i(TAG, "cache size="+allocatedSize+" length="+cache.size());
        if(allocatedSize>maxAllowedMemory){
        	Iterator<String> iter = cacheKeys.iterator();
        	while(iter.hasNext()){
        		String keyOfBitmap2Remove = iter.next();
        		RecyclingBitmapDrawable bmp = cache.get(keyOfBitmap2Remove);
                allocatedSize-=bmp.sizeInBytes/1024;
                iter.remove();
                cache.remove(keyOfBitmap2Remove);
                bmp.setIsCached(false);
                if(allocatedSize<=maxAllowedMemory)
                    break;
        	}
//            Iterator<Entry<String, Bitmap>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
//            while(iter.hasNext()){
//                Entry<String, Bitmap> entry=iter.next();
//                allocatedSize-=getSizeInBytes(entry.getValue());
//                iter.remove();
//                if(allocatedSize<=limit)
//                    break;
//            }
            Log.i(TAG, "Clean cache. New size "+cache.size());
        }
    }

    public void clear() {
        try{
        	 if (cache != null) {
             	Iterator<String> iter = cacheKeys.iterator();
            	while(iter.hasNext()){
            		String keyOfBitmap2Remove = iter.next();
            		RecyclingBitmapDrawable bmp = cache.get(keyOfBitmap2Remove);
//                    allocatedSize-=bmp.sizeInBytes/1024;
//                    iter.remove();
//                    cache.remove(keyOfBitmap2Remove);
                    bmp.setIsCached(false);
//                    if(allocatedSize<=maxAllowedMemory)
//                        break;
            	}
        		 
        		 cache.evictAll();
        		 cacheKeys.clear();
                 if (BuildConfig.DEBUG) {
                     Log.d(TAG, "Memory cache cleared");
                 }
        	 }
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
//        	if (cache!=null)
//        	for (Bitmap bm:cache.values())
//        		if (bm!=null)
//        			bm.recycle();
//            cache.clear();
        	
            allocatedSize=0;
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }

    public static void release() {
    	if (memoryCacheInstance!=null)
    		memoryCacheInstance.clear();
    	memoryCacheInstance = null;
    }    
    
//    @SuppressLint("NewApi")
//	private int getSizeInKBytes(Bitmap bitmap) {
//        if(bitmap==null)
//            return 0;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
//            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
//        } else {
//            return bitmap.getByteCount() / 1024;
//        }
//    }
}
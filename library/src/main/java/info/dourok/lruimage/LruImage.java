package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;


public abstract class LruImage {
    public final static int CACHE_LEVEL_NO_CACHE = 0;
    public final static int CACHE_LEVEL_MEMORY_CACHE = 0x1;
    public final static int CACHE_LEVEL_DISK_CACHE = 0x2;
    protected OnProgressUpdateListener progressListener;
    protected int mCacheLevel = CACHE_LEVEL_MEMORY_CACHE;
    protected LruCache<String, Bitmap> mLruCache;
    protected DiskLruCache mDiskLruCache;

    public static boolean isValid(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled();
    }

    /**
     * 子类读取原始图片的方法
     *
     * @return bitmap from source, or null if bitmap can't be load.
     */
    protected abstract Bitmap loadBitmap(Context context) throws LruImageException;


    public final Bitmap getBitmap(Context context) throws LruImageException {
        Log.d("LruImage", "getBitmap");
        Bitmap bitmap = null;
        String key = getKey();
        if (isUsingMemoryCache()) {
            bitmap = getBitmapFromMemory(key);
            if (isValid(bitmap)) {
                Log.d("LruImage", key + " Hit Memory");
            }
        }
        if (!isValid(bitmap)) {
            if (isUsingDiskCache()) {
                bitmap = getBitmapFromDisk(key);
                if (isValid(bitmap)) {
                    Log.d("LruImage", key + " Hit Disk");
                }
            }
            if (!isValid(bitmap)) {
                Log.d("LruImage", key + " No Hit");
                bitmap = loadBitmap(context);
                if (isValid(bitmap) && isUsingDiskCache()) {
                    saveBitmapToDisk(bitmap);
                }
            }
            if (isValid(bitmap) && isUsingMemoryCache()) {
                saveBitmapToMemory(bitmap);
            }
        }
        return bitmap;
    }

    void setProgressListener(OnProgressUpdateListener listener) {
        this.progressListener = listener;
    }

    protected void progressUpdate(int total, int position) {
        if (progressListener != null) {
            progressListener.onProgressUpdate(this, total, position);
        }
    }

    /**
     * @return Bitmap in LruCache, Null if no cache
     */
    public final Bitmap cacheMemory() {
        Bitmap bitmap = getBitmapFromMemory(getKey());
        return bitmap;
    }


    protected Bitmap getBitmapFromMemory(String key) {
        LruCache<String, Bitmap> lruCache = getLruCache();
        return lruCache.get(key);
    }

    /**
     * put bitmap into LruCache
     *
     * @param bitmap
     */
    protected void saveBitmapToMemory(Bitmap bitmap) {
        Log.d("LruImage", "saveBitmapToMemory");
        LruCache<String, Bitmap> lruCache = getLruCache();
        lruCache.put(getKey(), bitmap);
    }


    /**
     * Bitmap in DiskCache
     *
     * @return
     * @throws LruImageException
     */
    public final Bitmap cacheDisk() throws LruImageException {
        Bitmap bitmap = getBitmapFromDisk(getKey());
        if (isValid(bitmap) && isUsingMemoryCache()) {
            saveBitmapToMemory(bitmap);
        }
        return bitmap;
    }


    /**
     * FIXME is thread safe?
     *
     * @param key
     * @return
     */
    protected Bitmap getBitmapFromDisk(String key) {
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                return BitmapFactory.decodeStream(snapshot.getInputStream(0));
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean saveBitmapToDisk(Bitmap bitmap) {
        Log.d("LruImage", "saveBitmapToDisk");
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(getKey());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, editor.newOutputStream(0));
            editor.commit();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return is this image is exist on disk
     */
    public final boolean isCacheOnDisk() {
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(getKey());
            if (snapshot != null) {
                snapshot.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Each key must match the regex [a-z0-9_-]{1,64} as restrict by disk lru cache
     *
     * @return
     */
    public abstract String getKey();

    public final int getCacheLevel() {
        return mCacheLevel;
    }

    protected void setCacheLevel(int cacheLevel) {
        mCacheLevel = cacheLevel;
    }

    public boolean isUsingMemoryCache() {
        return (mCacheLevel & CACHE_LEVEL_MEMORY_CACHE) != 0;
    }

    public boolean isUsingDiskCache() {
        return (mCacheLevel & CACHE_LEVEL_DISK_CACHE) != 0;
    }

    /**
     * 如果没有指定的 LruCache，LruImage 将使用 LruCacheManager 的 LruCache
     *
     * @return
     */
    public LruCache<String, Bitmap> getLruCache() {
        return mLruCache;
    }

    protected void setLruCache(LruCache<String, Bitmap> lruCache) {
        this.mLruCache = lruCache;
    }

    public DiskLruCache getDiskLruCache() {
        return mDiskLruCache;
    }

    protected void setDiskLruCache(DiskLruCache diskLruCache) {
        this.mDiskLruCache = diskLruCache;
    }


    public interface OnProgressUpdateListener {
        void onProgressUpdate(LruImage image, int total, int position);
    }

}

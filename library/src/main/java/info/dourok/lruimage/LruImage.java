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
    private int mCacheLevel = CACHE_LEVEL_MEMORY_CACHE;
    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;

    public interface OnProgressUpdateListener {
        void onProgressUpdate(LruImage image, int total, int position);
    }

    protected OnProgressUpdateListener progressListener;


    /**
     * 子类读取原始图片的方法
     *
     * @return bitmap from source, or null if bitmap can't be load.
     */
    protected abstract Bitmap loadBitmap(Context context) throws LruImageException;


    public final Bitmap getBitmap(Context context) throws LruImageException {
        Bitmap bitmap = null;
        String key = getKey();
        if (hasUsingMemoryCache()) {
            bitmap = getBitmapFromMemory(key);
            if (isValid(bitmap)) {
                Log.d("LruImage", key + " Hit Memory");
            }
        }
        if (!isValid(bitmap)) {
            if (hasUsingDiskCache()) {
                bitmap = getBitmapFromDisk(context, key);
                if (isValid(bitmap)) {
                    Log.d("LruImage", key + " Hit Disk");
                }
            }
            if (!isValid(bitmap)) {
                Log.d("LruImage", key + " No Hit");
                bitmap = loadBitmap(context);
                if (isValid(bitmap) && hasUsingDiskCache()) {
                    saveBitmapToDisk(context, bitmap);
                }
            }
            if (isValid(bitmap) && hasUsingMemoryCache()) {
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


    protected synchronized final Bitmap getBitmapFromMemory(String key) {
        LruCache<String, Bitmap> lruCache = getLruCache() == null ? getDefaultLruCache() : getLruCache();
        return lruCache.get(key);
    }

    public final Bitmap cacheMemory() {
        Bitmap bitmap = getBitmapFromMemory(getKey());
        return bitmap;
    }

    protected synchronized final void saveBitmapToMemory(Bitmap bitmap) {
        Log.d("LruImage", "saveBitmapToMemory");
        LruCache<String, Bitmap> lruCache = getLruCache() == null ? getDefaultLruCache() : getLruCache();
        lruCache.put(getKey(), bitmap);
    }


    protected final synchronized Bitmap getBitmapFromDisk(Context context, String key) {
        DiskLruCache diskLruCache;
        try {
            diskLruCache = getDiskLruCache() == null ? getDefaultDiskLruCache(context) : getDiskLruCache();
            DiskLruCache.Snapshot snapshot = diskLruCache.get(getKey());
            if (snapshot != null) {
                return BitmapFactory.decodeStream(snapshot.getInputStream(0));
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected synchronized final boolean saveBitmapToDisk(Context context, Bitmap bitmap) {
        Log.d("LruImage", "saveBitmapToDisk");
        try {
            DiskLruCache diskLruCache = getDiskLruCache() == null ? getDefaultDiskLruCache(context) : getDiskLruCache();
            DiskLruCache.Editor editor = diskLruCache.edit(getKey());

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, editor.newOutputStream(0));
            editor.commit();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public final Bitmap cacheDisk(Context context) throws LruImageException {
        return getBitmapFromDisk(context, getKey());
    }

    public static boolean isValid(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled();
    }

    public DiskLruCache getDefaultDiskLruCache(Context context) throws IOException {
        return CacheManager.getInstance().getDefaultDiskCache(context);
    }

    protected final LruCache<String, Bitmap> getDefaultLruCache() {
        return CacheManager.getInstance().getDefaultMemoryCache();
    }

    /**
     * Each key must match the regex [a-z0-9_-]{1,64} as request in disk lru cache
     *
     * @return
     */
    public abstract String getKey();

    public final int getCacheLevel() {
        return mCacheLevel;
    }

    public final void setCacheLevel(int level) {
        mCacheLevel = level;
    }

    public final void addCacheLevel(int level) {
        mCacheLevel |= level;
    }

    public boolean hasUsingMemoryCache() {
        return (mCacheLevel & CACHE_LEVEL_MEMORY_CACHE) != 0;
    }

    public boolean hasUsingDiskCache() {
        return (mCacheLevel & CACHE_LEVEL_DISK_CACHE) != 0;
    }

    /**
     * 如果没有指定的 LruCache，LruImage 将使用 CacheManager 的 LruCache
     *
     * @return
     */
    public LruCache<String, Bitmap> getLruCache() {
        return mLruCache;
    }

    public void setLruCache(LruCache<String, Bitmap> lruCache) {
        this.mLruCache = lruCache;
    }

    public DiskLruCache getDiskLruCache() {
        return mDiskLruCache;
    }

    public void setDiskLruCache(DiskLruCache diskLruCache) {
        this.mDiskLruCache = diskLruCache;
    }
}
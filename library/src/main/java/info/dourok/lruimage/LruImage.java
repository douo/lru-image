package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;


public abstract class LruImage {
    public final static int CACHE_LEVEL_NO_CACHE = -1;
    public final static int CACHE_LEVEL_MEMORY_CACHE = 0;
    public final static int CACHE_LEVEL_DISK_CACHE = 1;

    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;

    /**
     * 子类读取原始图片的方法
     *
     * @return bitmap from source, or null if bitmap can't load.
     */
    protected abstract Bitmap loadBitmap(Context context) throws LruImageException;

    public final Bitmap getBitmap(Context context) throws LruImageException {
        switch (getCacheLevel()) {
            case CACHE_LEVEL_NO_CACHE:
                return loadBitmap(context);
            case CACHE_LEVEL_MEMORY_CACHE:
                return cacheMemory(context);
            case CACHE_LEVEL_DISK_CACHE:
                return cacheDisk(context);
            default:
                return loadBitmap(context);
        }
    }


    public final Bitmap cacheMemory(Context context) throws LruImageException {
        LruCache<String, Bitmap> lruCache = getLruCache() == null ? getDefaultLruCache() : getLruCache();
        Bitmap bitmap = lruCache.get(getKey());
        if (bitmap == null) {
            bitmap = loadBitmap(context);
            if (bitmap != null) {
                lruCache.put(getKey(), bitmap);
            }
        }
        return bitmap;
    }

    public final synchronized Bitmap cacheDisk(Context context) throws LruImageException {

        DiskLruCache diskLruCache = null;
        try {
            diskLruCache = getDiskLruCache() == null ? getDefaultDiskLruCache(context) : getDiskLruCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (diskLruCache != null) {
            LruCache<String, Bitmap> lruCache = getLruCache() == null ? getDefaultLruCache() : getLruCache();
            Bitmap bitmap = lruCache.get(getKey());
            if (bitmap == null) {// 如果不在内存里，尝试在储存器缓存里找
                try {
                    DiskLruCache.Snapshot snapshot = diskLruCache.get(getKey());
                    if (snapshot != null) {
                        bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    throw new LruImageException(e);
                }
            }
            if (bitmap == null) {//如果在储存器里也在找不到，从来源读取
                bitmap = loadBitmap(context);
                if (bitmap != null) { //
                    lruCache.put(getKey(), bitmap);
                    try {
                        DiskLruCache.Editor editor = diskLruCache.edit(getKey());
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, editor.newOutputStream(0));
                        editor.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                lruCache.put(getKey(), bitmap);
            }
            return bitmap;
        } else {
            Log.w("LruImage", "default DiskLruCache is null");
            return cacheMemory(context);
        }
    }

    public DiskLruCache getDefaultDiskLruCache(Context context) throws IOException {
        return CacheManager.getInstance().getDefaultDiskCache(context);
    }

    protected final LruCache<String, Bitmap> getDefaultLruCache() {
        return CacheManager.getInstance().getDefaultMemoryCache();
    }

    /**
     * Each key must match the regex [a-z0-9_-]{1,120} as request in disk lru cache
     *
     * @return
     */
    public abstract String getKey();

    public int getCacheLevel() {
        return CACHE_LEVEL_MEMORY_CACHE;
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
package info.dourok.lruimage;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

/**
 * Created by charry on 2014/11/20.
 */
public class CacheManager {
    private LruCache<String, Bitmap> mDefaultMemoryCache;
    private DiskLruCache mDefaultDiskCache;
    private static CacheManager singleton;
    private int mCacheSize;
    private String mDiskCacheFolder = "info.dourok.lruimage";
    private int mDiskCacheMaxSize = 50 * 1024 * 1024;

    public static CacheManager getInstance() {
        if (singleton == null) {
            singleton = new CacheManager();
        }
        return singleton;
    }

    private void initDefaultMemoryCache() {
        final int cacheSize;
        if (mCacheSize <= 0) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            //默认使用 1/8 的最大内存
            cacheSize = maxMemory / 8;
        } else {
            cacheSize = mCacheSize;
        }
        mDefaultMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //已 kb 为单位
                return value.getByteCount() / 1024;
            }
        };
    }

    private void initDefaultDiskCache(Context context) throws IOException {
        mDefaultDiskCache = DiskLruCache.open(getDiskCacheDir(context, mDiskCacheFolder),
                getAppVersion(context), 1, mDiskCacheMaxSize);
    }


    public LruCache<String, Bitmap> getDefaultMemoryCache() {
        if (mDefaultMemoryCache == null) {
            initDefaultMemoryCache();
        }
        return mDefaultMemoryCache;
    }

    public DiskLruCache getDefaultDiskCache(Context context) throws IOException {
        if (mDefaultDiskCache == null) {
            initDefaultDiskCache(context);
        }
        return mDefaultDiskCache;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
// but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }


    /**
     * 在 CacheManager 初始化 Cache 之前调用
     * 建议在 Application 的 onCreate 中调用。
     * @param cacheSize
     */
    public void setCacheSize(int cacheSize) {
        this.mCacheSize = cacheSize;
    }
    public void setDiskCacheFolder(String diskCacheFolder){
        this.mDiskCacheFolder = diskCacheFolder;
    }

    public void setDiskCacheMaxSize(int diskCacheMaxSize){
        this.mDiskCacheMaxSize = diskCacheMaxSize;
    }
}

package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by larry on 5/29/16.
 */
public class LruTaskBuilder {

    private static final int CACHE_NO_SET = -1;
    private static final int LOADING_THREADS = 4;
    private static ExecutorService DEFAULT_DISK_LOADER = Executors.newFixedThreadPool(LOADING_THREADS);
    private static ExecutorService DEFAULT_IMAGE_LOADER = Executors.newFixedThreadPool(LOADING_THREADS);
    private LruImageTask.OnCompleteListener mListener;
    private LruImage.OnProgressUpdateListener mProgressListener;
    private SuccessCallback mSuccess;
    private FailureCallback mFailure;
    private CancelCallback mCancel;
    private int mCacheLevel;
    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;
    private Context mContext;
    private ExecutorService mImageLoader;
    private ExecutorService mDiskCacheLoader;

    public LruTaskBuilder(Context context) {
        mContext = context;
        mCacheLevel = CACHE_NO_SET;
    }

    public static void cancelAllTasksInDefaultExecutor() {
        DEFAULT_IMAGE_LOADER.shutdownNow();
        DEFAULT_IMAGE_LOADER = Executors.newFixedThreadPool(LOADING_THREADS);
    }

    public LruTaskBuilder setDiskLruCache(DiskLruCache diskLruCache) {
        mDiskLruCache = diskLruCache;
        return getThis();
    }

    public final LruTaskBuilder setCacheLevel(int level) {
        mCacheLevel = level;
        return getThis();
    }

    public final LruTaskBuilder addCacheLevel(int level) {
        mCacheLevel |= level;
        return getThis();
    }

    public LruTaskBuilder setLruCache(LruCache<String, Bitmap> lruCache) {
        mLruCache = lruCache;
        return getThis();
    }

    public LruTaskBuilder setOnCompleteListener(LruImageTask.OnCompleteListener listener) {
        mListener = listener;
        return getThis();
    }

    public LruTaskBuilder success(SuccessCallback callback) {
        mSuccess = callback;
        return getThis();
    }

    public LruTaskBuilder failure(FailureCallback callback) {
        mFailure = callback;
        return getThis();
    }

    public LruTaskBuilder cancel(CancelCallback callback) {
        mCancel = callback;
        return getThis();
    }

    public LruTaskBuilder progress(LruImage.OnProgressUpdateListener callback) {
        mProgressListener = callback;
        return getThis();
    }

    public LruTaskBuilder setImageLoader(ExecutorService loader) {
        mImageLoader = loader;
        return getThis();
    }

    public LruTaskBuilder setDiskCacheLoader(ExecutorService loader) {
        mDiskCacheLoader = loader;
        return getThis();
    }

    protected DiskLruCache getDefaultDiskLruCache(Context context) throws IOException {
        return LruCacheManager.getInstance().getDefaultDiskCache(context);
    }

    protected final LruCache<String, Bitmap> getDefaultLruCache() {
        return LruCacheManager.getInstance().getDefaultMemoryCache();
    }

    public LruTaskBuilder getThis() {
        return this;
    }

    public LruImageTask build(LruImage image) {

        if (mCacheLevel != CACHE_NO_SET) {
            image.setCacheLevel(mCacheLevel);
        }

        if (mLruCache != null) {
            image.setLruCache(mLruCache);
        } else if (image.getLruCache() == null) {
            image.setLruCache(getDefaultLruCache());
        }

        if (mDiskLruCache != null) {
            image.setDiskLruCache(mDiskLruCache);
        } else if (image.getDiskLruCache() == null) {
            try {
                image.setDiskLruCache(getDefaultDiskLruCache(mContext));
            } catch (IOException e) {
                Log.w("LruImage", "Default disk cache init failed!");
                e.printStackTrace();
            }
        }
        /*
               Task params
        */
        if (mListener == null) {
            mListener = new LruImageTask.OnCompleteListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    if (mSuccess != null) {
                        mSuccess.call(bitmap);
                    }
                }

                @Override
                public void onFailure(LruImageException e) {
                    if (mFailure != null) {
                        mFailure.call(e);
                    }
                }

                @Override
                public void cancel() {
                    if (mCancel != null) {
                        mCancel.call();
                    }
                }
            };
        }

        if (mImageLoader == null) {
            mImageLoader = DEFAULT_IMAGE_LOADER;
        }
        if (mDiskCacheLoader == null) {
            mDiskCacheLoader = DEFAULT_DISK_LOADER;
        }

        return new LruImageTask(mContext, image, mImageLoader, mDiskCacheLoader, mListener, mProgressListener);
    }

    public LruImageTask execute(LruImage image) {
        return build(image).execute();
    }

    public interface SuccessCallback {
        void call(Bitmap bitmap);
    }

    public interface FailureCallback {
        void call(LruImageException bitmap);
    }


    public interface CancelCallback {
        void call();
    }
}

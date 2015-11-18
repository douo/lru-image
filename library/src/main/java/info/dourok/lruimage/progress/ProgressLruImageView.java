package info.dourok.lruimage.progress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;

import info.dourok.lruimage.BufferWebImage;
import info.dourok.lruimage.LruImage;
import info.dourok.lruimage.LruImageException;
import info.dourok.lruimage.LruImageTask;

/**
 * Created by DouO on 2015/10/25.
 */
public class ProgressLruImageView extends ImageView {

    protected LruImageTask currentTask;

    public ProgressLruImageView(Context context) {
        super(context);
    }

    public ProgressLruImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressLruImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Drawable buildDefaultProgressDrawable() {
        return new CircleProgressDrawable(getContext());
    }

    // Helpers to set image by URL
    public void setImageUrl(String url) {
        setImage(new BufferWebImage(url));
    }

    // Helpers to set image by URL
    public void setImageUrl(String url, int reqWidth, int reqHeight) {
        setImage(new BufferWebImage(url, reqWidth, reqHeight));
    }

    public void setImageUrl(String url, int reqWidth, int reqHeight, boolean reqSize, int cacheLevel) {
        setImage(new BufferWebImage(url, reqWidth, reqHeight, reqSize, cacheLevel));
    }

    public void setImageUrl(String url, int reqWidth, int reqHeight, boolean reqSize, int cacheLevel, final Integer fallbackResource, final Integer loadingResource, final LruImageTask.OnCompleteListener completeListener) {
        setImage(new BufferWebImage(url, reqWidth, reqHeight, reqSize, cacheLevel), fallbackResource, loadingResource, completeListener);
    }

    public void setImageUrl(String url, LruImageTask.OnCompleteListener completeListener) {
        setImage(new BufferWebImage(url), completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource) {
        setImage(new BufferWebImage(url), fallbackResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, LruImageTask.OnCompleteListener completeListener) {
        setImage(new BufferWebImage(url), fallbackResource, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource) {
        setImage(new BufferWebImage(url), fallbackResource, loadingResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, LruImageTask.OnCompleteListener completeListener) {
        setImage(new BufferWebImage(url), fallbackResource, loadingResource, completeListener);
    }

    // Set image using LruImage object
    public void setImage(final LruImage image) {
        setImage(image, null, null, null);
    }

    public void setImage(final LruImage image, final LruImageTask.OnCompleteListener completeListener) {
        setImage(image, null, null, completeListener);
    }

    public void setImage(final LruImage image, final Integer fallbackResource) {
        setImage(image, fallbackResource, fallbackResource, null);
    }

    public void setImage(final LruImage image, final Integer fallbackResource, LruImageTask.OnCompleteListener completeListener) {
        setImage(image, fallbackResource, fallbackResource, completeListener);
    }

    public void setImage(final LruImage image, final Integer fallbackResource, final Integer loadingResource) {
        setImage(image, fallbackResource, loadingResource, null);
    }

    private ScaleType originScaleType;
    private boolean startLoading;

    public void setImage(final LruImage image, final Integer fallbackResource, final Integer loadingResource, final LruImageTask.OnCompleteListener completeListener) {
        // Set a loading resource
        if (loadingResource != null && loadingResource != 0) {
            setImageResource(loadingResource);
        }
        // Cancel any existing tasks for this image view
        cancelTaskIfNecessary();

        // Set up the new task
        currentTask = new LruImageTask(getContext(), image, getLoader(), new LruImageTask.OnCompleteListener() {
            @Override
            public void onSuccess(LruImage image, Bitmap bitmap) {
                if (originScaleType != null) {
                    setScaleType(originScaleType);
                    originScaleType = null;
                }
                startLoading = false;
                setImageBitmap(bitmap);
                if (completeListener != null) {
                    completeListener.onSuccess(image, bitmap);
                }

            }

            @Override
            public void onFailure(LruImage image, LruImageException e) {
                if (originScaleType != null) {
                    setScaleType(originScaleType);
                    originScaleType = null;
                }
                startLoading = false;
                if (fallbackResource != null) {
                    setImageResource(fallbackResource);
                }
                if (completeListener != null) {
                    completeListener.onFailure(image, e);
                }
            }

            @Override
            public void cancel() {
                if (originScaleType != null) {
                    setScaleType(originScaleType);
                    originScaleType = null;
                }
                startLoading = false;
                if (completeListener != null) {
                    completeListener.cancel();
                }
                startLoading = false;
            }
        }, new LruImage.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(LruImage image, int total, int position) {
                if (!startLoading) {
                    startLoading = true;
                    setImageDrawable(buildDefaultProgressDrawable());
                    originScaleType = getScaleType();
                    setScaleType(ScaleType.CENTER);
                }
                if (total != 0) {
                    setImageLevel((int) (0.9f * position / total * ProgressDrawableBase.LEVEL_MAX));
                }
            }
        });
        currentTask.execute();
    }


    private void cancelTaskIfNecessary() {
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
        setImageDrawable(null);
        if (originScaleType != null) {
            setScaleType(originScaleType);
            originScaleType = null;
        }
        startLoading = false;
    }


    private void d(String msg) {
        Log.d("LruImageView", msg);
    }

    protected ExecutorService mLoader;

    public ExecutorService getLoader() {
        return mLoader;
    }

    public void setLoader(ExecutorService loader) {
        this.mLoader = loader;
    }
}

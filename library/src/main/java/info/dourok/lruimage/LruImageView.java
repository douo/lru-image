package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;

import info.dourok.lruimage.progress.CircleProgressDrawable;
import info.dourok.lruimage.progress.ProgressDrawableBase;

/**
 * Created by DouO on 2015/10/25.
 */
public class LruImageView extends ImageView {

    protected LruImageTask currentTask;
    protected ExecutorService mLoader;
    private ScaleType originScaleType;
    private boolean mShowProgress;
    private boolean startLoading;
    private LruTaskBuilder mBuilder;

    private ProgressDrawableBase mProgressDrawable;
    @DrawableRes
    private int mFallbackResource;

    public LruImageView(Context context) {
        super(context);
    }

    public LruImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LruImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isShowProgress() {
        return mShowProgress;
    }

    /**
     * 下载过程中将当前 Drawable 切换到 progress drawable
     * progress drawable 通过 ImageLevel 来显示下载进度
     * ProgressDrawble 会将 ImageView 的 ScaleType 固定为 ScaleType.CENTER
     * 将原来的 ScaleType 保存在 originScaleType
     * 图片下载完毕要显示图片时再切换回来。
     *
     * @param showProgress 是否显示下载进度
     */
    public void setShowProgress(boolean showProgress) {
        if (this.mShowProgress != showProgress) {
            mShowProgress = showProgress;
            if (showProgress) {
                if (getProgressDrawable() == null) {
                    setProgressDrawable(buildDefaultProgressDrawable());
                }
            }
        }

    }

    public ProgressDrawableBase getProgressDrawable() {
        return mProgressDrawable;
    }

    public void setProgressDrawable(ProgressDrawableBase progressDrawable) {
        if (mProgressDrawable != progressDrawable) {
            mProgressDrawable = progressDrawable;
        }
    }

    private ProgressDrawableBase buildDefaultProgressDrawable() {
        return new CircleProgressDrawable(getContext());
    }

    public void setFallbackResource(@DrawableRes int resId) {
        mFallbackResource = resId;
    }

    public void setTaskBuilder(LruTaskBuilder builder) {
        mBuilder = builder;
    }


    /**
     * Helpers to set image by URL
     * TODO support more protocol
     * @param url
     */
    //
    public void setImageUrl(String url) {
        setImage(new WebImage(url));
    }


    public void setImageUrl(String url, LruImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), completeListener);
    }


    // Set image using LruImage object
    public void setImage(final LruImage image) {
        setImage(image, null);
    }


    public void setImage(final LruImage image, final LruImageTask.OnCompleteListener completeListener) {
        // Cancel any existing tasks for this image view
        cancelTaskIfNecessary();
        // Set up the new task
        currentTask = getBuilder().setOnCompleteListener(new LruImageTask.OnCompleteListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (originScaleType != null) {
                    setScaleType(originScaleType);
                    originScaleType = null;
                }
                startLoading = false;
                setLruBitmap(image, bitmap);
                if (completeListener != null) {
                    completeListener.onSuccess(bitmap);
                }

            }

            @Override
            public void onFailure(LruImageException e) {
                if (originScaleType != null) {
                    setScaleType(originScaleType);
                    originScaleType = null;
                }
                startLoading = false;
                if (mFallbackResource != 0) {
                    setImageResource(mFallbackResource);
                }
                if (completeListener != null) {
                    completeListener.onFailure(e);
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
        }).progress(isShowProgress() ? new LruImage.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(LruImage image, int total, int position) {
                if (!startLoading) {
                    startLoading = true;
                    setImageDrawable(mProgressDrawable);
                    setImageLevel(0);
                    originScaleType = getScaleType();
                    setScaleType(ScaleType.CENTER);
                }
                if (total != 0) {
                    setImageLevel((int) (0.99f * position / total * ProgressDrawableBase.LEVEL_MAX));
                }
            }
        } : null).execute(image);

    }


    private LruTaskBuilder getBuilder() {
        if (mBuilder == null) {
            mBuilder = new LruTaskBuilder(getContext()).setImageLoader(getLoader());
        }
        return mBuilder;
    }

    /**
     * @param image
     * @param bitmap
     */
    @Deprecated
    protected void setLruBitmap(LruImage image, Bitmap bitmap) {
        setImageBitmap(bitmap);
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

    public ExecutorService getLoader() {
        return mLoader;
    }

    public void setLoader(ExecutorService loader) {
        this.mLoader = loader;
    }
}

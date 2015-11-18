package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;

/**
 * Created by charry on 2014/11/20.
 */
public class LruImageView extends ImageView {


    protected LruImageTask currentTask;

    public LruImageView(Context context) {
        super(context);
    }

    public LruImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LruImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    // Helpers to set image by URL
    public void setImageUrl(String url) {
        setImage(new WebImage(url));
    }

    // Helpers to set image by URL
    public void setImageUrl(String url, int reqWidth, int reqHeight) {
        setImage(new WebImage(url, reqWidth, reqHeight));
    }

    public void setImageUrl(String url, int reqWidth, int reqHeight, boolean reqSize, int cacheLevel) {
        setImage(new WebImage(url, reqWidth, reqHeight, reqSize, cacheLevel));
    }

    public void setImageUrl(String url, int reqWidth, int reqHeight, boolean reqSize, int cacheLevel, final Integer fallbackResource, final Integer loadingResource, final LruImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url, reqWidth, reqHeight, reqSize, cacheLevel), fallbackResource, loadingResource, completeListener);
    }

    public void setImageUrl(String url, LruImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource) {
        setImage(new WebImage(url), fallbackResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, LruImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource) {
        setImage(new WebImage(url), fallbackResource, loadingResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, LruImageTask.OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, loadingResource, completeListener);
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

    public void setImage(final LruImage image, final Integer fallbackResource, final Integer loadingResource, final LruImageTask.OnCompleteListener completeListener) {
        // Set a loading resource
        if (loadingResource != null) {
            setImageResource(loadingResource);
        }

        // Cancel any existing tasks for this image view
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }

        // Set up the new task
        currentTask = new LruImageTask(getContext(), image, getLoader(), new LruImageTask.OnCompleteListener() {
            @Override
            public void onSuccess(LruImage image, Bitmap bitmap) {
                setLruBitmap(image, bitmap);
                if (completeListener != null) {
                    completeListener.onSuccess(image, bitmap);
                }
            }

            @Override
            public void onFailure(LruImage image, LruImageException e) {
                if (fallbackResource != null) {
                    setImageResource(fallbackResource);
                }
                if (completeListener != null) {
                    completeListener.onFailure(image, e);
                }
            }

            @Override
            public void cancel() {
                if (completeListener != null) {
                    completeListener.cancel();
                }
            }
        });
        currentTask.execute();
    }

    protected void setLruBitmap(LruImage image, Bitmap bitmap) {
        setImageBitmap(bitmap);
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

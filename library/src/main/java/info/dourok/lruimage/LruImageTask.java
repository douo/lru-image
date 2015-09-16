package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by charry on 2014/11/20.
 */
public class LruImageTask implements Runnable {
    private static final int LOADING_THREADS = 4;

    private static ExecutorService DEFAULT_LOADER = Executors.newFixedThreadPool(LOADING_THREADS);

    public static void cancelAllTasksInDefaultExecutor() {
        DEFAULT_LOADER.shutdownNow();
        DEFAULT_LOADER = Executors.newFixedThreadPool(LOADING_THREADS);
    }

    private ExecutorService mLoader;

    private static final int BITMAP_READY = 0;
    private static final int BITMAP_FAILURE = -1;
    private static final int BITMAP_CANCEL = 1;

    private OnCompleteHandler onCompleteHandler;
    private LruImage image;
    private Context context;
    private int priority;
    private Future<?> future;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (this.priority != priority) {
            this.priority = priority;
            if (getLoader() instanceof LruImagePriorityLoader) {
                // 基于堆实现的优先队列，要更改集合内元素的优先度，得移出后再添加
                LruImagePriorityLoader loader = (LruImagePriorityLoader) getLoader();
                if (future != null && !future.isCancelled() && !future.isDone()) {
                    cancel();
                    execute();
                }
            }
        }
    }

    private static class OnCompleteHandler extends Handler {
        private LruImageTask task;

        private OnCompleteHandler(LruImageTask task) {
            this.task = task;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BITMAP_READY:
                    if (task.listener != null) {
                        task.listener.onSuccess(task.image, (Bitmap) msg.obj);
                    }
                    break;
                case BITMAP_FAILURE:
                    if (task.listener != null) {
                        task.listener.onFailure(task.image, (LruImageException) msg.obj);
                    }
                    break;
                case BITMAP_CANCEL:
                    if (task.listener != null) {
                        task.listener.cancel();
                    }
            }
        }
    }

    public interface OnCompleteListener {
        void onSuccess(LruImage image, Bitmap bitmap);

        void onFailure(LruImage image, LruImageException e);

        void cancel();
    }

    public OnCompleteListener listener;

    public LruImageTask(Context context, LruImage image, OnCompleteListener listener) {
        this(context, image, DEFAULT_LOADER, listener);
    }

    public LruImageTask(Context context, LruImage image, ExecutorService loader, OnCompleteListener listener) {
        this.image = image;
        this.context = context;
        this.listener = listener;
        this.mLoader = loader == null ? DEFAULT_LOADER : loader;
        onCompleteHandler = new OnCompleteHandler(this);
    }

    @Override
    public void run() {
        if (image != null) {
            try {
                complete(image.getBitmap(context));
            } catch (LruImageException e) {
                e.printStackTrace();
                failure(e);
            }
            context = null;
        }
    }

    /**
     * 如果Bitmap在内存中，直接在当前线程返回
     *
     * @return
     */
    public LruImageTask execute() {
        if (image != null && image.getCacheLevel() >= LruImage.CACHE_LEVEL_MEMORY_CACHE) {
            Bitmap bitmap = image.cacheMemory();
            if (LruImage.isValid(bitmap)) {
                Log.d("LruImage", image.getKey() + " Loaded in UI Thread");
                listener.onSuccess(image, bitmap);
                return this;
            }
        }
        future = getLoader().submit(this);
        return this;
    }


    public void cancel(boolean mayInterruptIfRunning) {
        if (future != null) {
            if (future.cancel(mayInterruptIfRunning)) {
                onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(BITMAP_CANCEL, null));
            }
        }
    }

    public void cancel() {
        cancel(false);
    }

    public boolean isCancelled() {
        return future != null && future.isCancelled();
    }

    public boolean isDone() {
        return future != null && future.isDone();
    }

    public void complete(Bitmap bitmap) {
        if (onCompleteHandler != null && future != null && !future.isCancelled()) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(BITMAP_READY, bitmap));
        }
    }

    public void failure(LruImageException exp) {
        if (onCompleteHandler != null && future != null && !future.isCancelled()) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(BITMAP_FAILURE, exp));
        }
    }

    public ExecutorService getLoader() {
        return mLoader;
    }

    public void setLoader(ExecutorService loader) {
        this.mLoader = loader;
    }
}

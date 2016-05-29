package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by charry on 2014/11/20.
 */
public class LruImageTask implements Runnable, LruImage.OnProgressUpdateListener {

    private static final int BITMAP_READY = 0;
    private static final int BITMAP_FAILURE = -1;
    private static final int BITMAP_CANCEL = 1;
    private static final int PROGRESS = 0x10;
    public OnCompleteListener listener;
    public LruImage.OnProgressUpdateListener progressListener;
    private ExecutorService mImageLoader;
    private ExecutorService mDiskCacheLoader;
    private OnCompleteHandler onCompleteHandler;
    private LruImage image;
    private Context context;
    private Future<?> future;
    private int priority;


    public LruImageTask(Context context, LruImage image, ExecutorService loader, ExecutorService diskLoader, OnCompleteListener listener, LruImage.OnProgressUpdateListener pListener) {
        this.image = image;
        this.context = context;
        this.listener = listener;
        this.mImageLoader = loader;
        this.mDiskCacheLoader = diskLoader;
        onCompleteHandler = new OnCompleteHandler(this);
        if (pListener != null) {
            this.progressListener = pListener;
            image.setProgressListener(this);
        }
    }

    /*public*/ int getPriority() {
        return priority;
    }

    /*public*/ void setPriority(int priority) {
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

    @Override
    public void run() {
        try {
            complete(image.getBitmap(context));
        } catch (LruImageException e) {
            e.printStackTrace();
            failure(e);
        }
        context = null;
    }

    /**
     * 如果Bitmap在内存中，直接在当前线程返回
     *
     * @return
     */
    public LruImageTask execute() {
        if (image != null) {
            if (image.isUsingMemoryCache()) {
                Bitmap bitmap = image.cacheMemory();
                if (LruImage.isValid(bitmap)) {
                    Log.d("LruImage", image.getKey() + " Loaded in UI Thread");
                    listener.onSuccess(bitmap);
                    return this;
                }
            }
            if (image.isUsingDiskCache()) {
                future = getDiskLoader().submit(new DiskWorker());
            } else {
                future = getLoader().submit(this);
            }
        } else {
            throw new NullPointerException("LruImage can not be null");
        }
        return this;
    }

    public void cancel(boolean mayInterruptIfRunning) {
        if (future != null) {
            boolean b = future.cancel(mayInterruptIfRunning);
            Log.v("LruImageTask", "cancel:" + mayInterruptIfRunning + " " + b);
            if (b) {
                onCompleteHandler.removeMessages(PROGRESS);
                onCompleteHandler.removeMessages(BITMAP_READY);
                onCompleteHandler.removeMessages(BITMAP_FAILURE);
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

    @Override
    public void onProgressUpdate(LruImage image, int total, int position) {
        if (onCompleteHandler != null && future != null && !future.isCancelled()) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(PROGRESS, total, position));
        }
    }

    private ExecutorService getDiskLoader() {
        return mDiskCacheLoader;
    }

    public ExecutorService getLoader() {
        return mImageLoader;
    }

    public LruImage getImage() {
        return image;
    }

    public interface OnCompleteListener {
        void onSuccess(Bitmap bitmap);

        void onFailure(LruImageException e);

        void cancel();
    }

    private static class OnCompleteHandler extends Handler {
        private LruImageTask task;

        private OnCompleteHandler(LruImageTask task) {
            this.task = task;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    if (task.progressListener != null) {
                        task.progressListener.onProgressUpdate(task.image, msg.arg1, msg.arg2);
                    }
                    break;
                case BITMAP_READY:
                    if (task.listener != null) {
                        task.listener.onSuccess((Bitmap) msg.obj);
                    }
                    break;
                case BITMAP_FAILURE:
                    if (task.listener != null) {
                        task.listener.onFailure((LruImageException) msg.obj);
                    }
                    break;
                case BITMAP_CANCEL:
                    if (task.listener != null) {
                        task.listener.cancel();
                    }
                    task.listener = null;
                    task.progressListener = null;
                    break;
            }
        }
    }

    private class DiskWorker implements Runnable {

        @Override
        public void run() {
            Bitmap bitmap = null;
            try {
                bitmap = image.cacheDisk();
            } catch (LruImageException e) {
                e.printStackTrace();
            }
            if (LruImage.isValid(bitmap)) {
                complete(bitmap);
                Log.d("LruImageTask", image.getKey() + " Loaded from Disk");
            } else {
                future = getLoader().submit(LruImageTask.this);
            }
        }
    }
}

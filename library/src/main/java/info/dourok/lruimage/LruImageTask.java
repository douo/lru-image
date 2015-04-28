package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by charry on 2014/11/20.
 */
public class LruImageTask implements Runnable {
    private static final int LOADING_THREADS = 4;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);

    public static void execute(LruImageTask task) {
        threadPool.execute(task);
    }

    public static void cancelAllTasks() {
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    }


    private static final int BITMAP_READY = 0;
    private static final int BITMAP_FAILURE = -1;


    private boolean cancelled = false;
    private OnCompleteHandler onCompleteHandler;
    private LruImage image;
    private Context context;

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
            }
        }


    }

    public interface OnCompleteListener {
        void onSuccess(LruImage image, Bitmap bitmap);

        void onFailure(LruImage image, LruImageException e);
    }

    public OnCompleteListener listener;

    public LruImageTask(Context context, LruImage image, OnCompleteListener listener) {
        this.image = image;
        this.context = context;
        this.listener = listener;
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

    public void cancel() {
        cancelled = true;
    }

    public void complete(Bitmap bitmap) {
        if (onCompleteHandler != null && !cancelled) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(BITMAP_READY, bitmap));
        }
    }

    public void failure(LruImageException exp) {
        if (onCompleteHandler != null && !cancelled) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(BITMAP_FAILURE, exp));
        }
    }
}

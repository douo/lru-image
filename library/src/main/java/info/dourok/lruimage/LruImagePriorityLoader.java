package info.dourok.lruimage;

import android.os.Build;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by John on 2015/9/1.
 */
public class LruImagePriorityLoader extends ThreadPoolExecutor {
    private static final int LOADING_THREADS = 4;

    public LruImagePriorityLoader(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, buildQueue());
    }

    public LruImagePriorityLoader(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, buildQueue(), threadFactory);
    }


    public LruImagePriorityLoader(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, buildQueue(), handler);
    }

    public LruImagePriorityLoader(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit, ThreadFactory threadFactory,
                                  RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, buildQueue(), threadFactory, handler);
    }

    private static PriorityBlockingQueue<Runnable> buildQueue() {
        return new PriorityBlockingQueue<>(11, new Comparator<Runnable>() {
            @Override
            public int compare(Runnable lhs, Runnable rhs) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return Integer.compare(((LruImageTask) lhs).getPriority(), ((LruImageTask) rhs).getPriority());
                } else {
                    int _lhs = ((LruImageTask) lhs).getPriority(), _rhs = ((LruImageTask) rhs).getPriority();
                    return _lhs < _rhs ? -1 : (_lhs == _rhs ? 0 : 1);
                }
            }
        });
    }

    public static LruImagePriorityLoader newFixedThreadPool(int size) {
        return new LruImagePriorityLoader(size, size, 0L, TimeUnit.MILLISECONDS);
    }

}

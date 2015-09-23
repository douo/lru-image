package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by charry on 2014/11/18.
 */
public class WebImage extends LruImage {
    private String url;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private final static int NO_REQ_SIZE = Integer.MAX_VALUE;
    /**
     * 当任意一个 reqSize 的值不等于 -1 则对图片进行 inSampleSize 缩放
     */
    private int reqWidth;
    private int reqHeight;
    /**
     * 是否缩小到 reqSize 要求尺寸，false 则比 reqSize 大的最小 inSampleSize 的尺寸
     * 不会改变图片原有比例
     */
    private boolean reqSize;

    public WebImage(String url) {
        this(url, NO_REQ_SIZE, NO_REQ_SIZE, false);
    }

    public WebImage(String url, int cacheLevel) {
        this(url, NO_REQ_SIZE, NO_REQ_SIZE, false, cacheLevel);
    }

    public WebImage(String url, int reqWidth, int reqHeight) {
        this(url, reqWidth, reqHeight, false);
    }

    public WebImage(String url, int reqWidth, int reqHeight, boolean reqSize) {
        this(url, reqWidth, reqHeight, reqSize, CACHE_LEVEL_DISK_CACHE | CACHE_LEVEL_MEMORY_CACHE);
    }
    
    public WebImage(String url, int reqWidth, int reqHeight, boolean reqSize, int cacheLevel) {
        this.url = url;
        this.reqHeight = reqHeight;
        this.reqWidth = reqWidth;
        this.reqSize = reqSize;
        setCacheLevel(cacheLevel);
    }


    private URLConnection newConnection() throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        return conn;
    }

    /**
     * @param context
     * @return Bitmap from source, or null if any ioexception or bitmapfactory can't decode
     */
    @Override
    protected Bitmap loadBitmap(Context context) throws LruImageException {
        Bitmap bitmap = null;
        try {

            final BitmapFactory.Options options = new BitmapFactory.Options();

            if (reqWidth != NO_REQ_SIZE || reqHeight != NO_REQ_SIZE) {
                options.inJustDecodeBounds = true;
                URLConnection conn = newConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                conn = newConnection();
                conn.connect();
                is = conn.getInputStream();
                Bitmap _bitmap = BitmapFactory.decodeStream(is, null, options);
                if (reqSize && _bitmap.getWidth() > reqWidth && _bitmap.getHeight() > reqHeight) {
                    //If the specified width and height are the same as the current width and height of
                    //the source bitmap, the source bitmap is returned and no new bitmap is created.
                    float scale = Math.max(1.f * reqWidth / _bitmap.getWidth(), 1.f * reqHeight / _bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(_bitmap, (int) (_bitmap.getWidth() * scale), (int) (_bitmap.getHeight() * scale), false);
                    if (bitmap != _bitmap) {
                        _bitmap.recycle();
                    }
                } else {
                    bitmap = _bitmap;
                }
            } else {
                URLConnection conn = newConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new LruImageException(e);
        }
        return bitmap;
    }

    @Override
    public String getKey() {

        String s = reqHeight + url + reqWidth + "_" + (reqSize ? 1 : 0);
        return Integer.toHexString(s.hashCode());
        //s = s.replace(":", "_").replace("/", "_").replace(".", "_");
        //return s.substring(s.length() > 64 ? s.length() - 64 : 0, s.length());
    }
}

package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by charry on 2014/11/18.
 */
public class WebImage extends LruImage {
    private String url;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private int reqWidth = Integer.MAX_VALUE;
    private int reqHeight = Integer.MAX_VALUE;

    public WebImage(String url) {
        this.url = url;
    }

    public WebImage(String url, int reqWidth, int reqHeight) {
        this.url = url;
        this.reqHeight = reqHeight;
        this.reqWidth = reqWidth;
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

            if (reqWidth != Integer.MAX_VALUE || reqHeight != Integer.MAX_VALUE) {
                options.inJustDecodeBounds = true;
                URLConnection conn = newConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                Utils.calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                conn = newConnection();
                conn.connect();
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is, null, options);
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
    public int getCacheLevel() {
        return CACHE_LEVEL_DISK_CACHE;
    }

    @Override
    public String getKey() {
        return Integer.toHexString(url.hashCode());
    }
}

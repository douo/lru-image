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

    public WebImage(String url) {
        this.url = url;
    }

    /**
     * @param context
     * @return Bitmap from source, or null if any ioexception or bitmapfactory can't decode
     */
    @Override
    protected Bitmap loadBitmap(Context context) throws LruImageException {
        Bitmap bitmap = null;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            bitmap = BitmapFactory.decodeStream((InputStream) conn.getContent());
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

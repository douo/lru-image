package info.dourok.lruimage.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.widget.ImageView.ScaleType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import info.dourok.lruimage.LruImage;

/**
 * Created by John on 2015/9/23.
 * Modified from volley
 */
public class WebImage extends UrlImage {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private final String url;
    /**
     * load Bitmap from net to memory
     *
     * @return bytes of bitmap
     * @throws IOException
     */
    byte[] tempData;

    public WebImage(String url) {
        this(url, 0, 0, null, null);
    }


    public WebImage(String url, int maxWidth, int maxHeight,
                    ScaleType scaleType, Config decodeConfig) {
        super(maxWidth, maxHeight, scaleType, decodeConfig);
        this.url = url;
        setCacheLevel(LruImage.CACHE_LEVEL_DISK_CACHE | LruImage.CACHE_LEVEL_MEMORY_CACHE);
    }

    private URLConnection newConnection() throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        return conn;
    }

    @Override
    protected void prepareData(Context context) throws IOException {
        URLConnection conn = newConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte data[];
        data = new byte[8192];
        final int total = conn.getContentLength();
        int sum = 0;
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
            progressUpdate(total, sum += n);
        }
        buffer.flush();
        tempData = buffer.toByteArray();
    }

    @Override
    protected Bitmap decodingBitmap(Context context, BitmapFactory.Options decodeOptions) {
        return BitmapFactory.decodeByteArray(tempData, 0, tempData.length, decodeOptions);
    }

    @Override
    protected void onDecodeFinish() {
        tempData = null;
    }

    @Override
    public String getKey() {
        String s = url + super.getKey();
        return Integer.toHexString(s.hashCode());
        //s = s.replace(":", "_").replace("/", "_").replace(".", "_");
        //return s.substring(s.length() > 64 ? s.length() - 64 : 0, s.length());
    }
}

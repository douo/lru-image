package info.dourok.lruimage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.widget.ImageView.ScaleType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by John on 2015/9/23.
 * Modified from volley
 */
public class WebImage extends ScalableImage {

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
        setCacheLevel(CACHE_LEVEL_DISK_CACHE | CACHE_LEVEL_MEMORY_CACHE);
    }

    private URLConnection newConnection() throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        return conn;
    }

    @Override
    protected void prepareData() throws IOException {
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
    protected Bitmap decodingBitmap(BitmapFactory.Options decodeOptions) {
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

    public static class Builder {
        private Config decodeConfig;
        private int maxWidth;
        private int maxHeight;
        private String url;
        private ScaleType scaleType;

        public Builder(String url) {
            this.url = url;
        }

        public Builder setDecodeConfig(Config decodeConfig) {
            this.decodeConfig = decodeConfig;
            return this;
        }

        public Builder setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder setMaxSize(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * @param scaleType 只有 CENTER_CROP 和 FIX_XY 能起效果
         * @return
         */
        public Builder setScaleType(ScaleType scaleType) {
            this.scaleType = scaleType;
            return this;
        }

        public WebImage create() {
            return new WebImage(url, maxWidth, maxHeight, scaleType, decodeConfig);
        }
    }
}

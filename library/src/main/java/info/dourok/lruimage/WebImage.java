package info.dourok.lruimage;

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

/**
 * Created by John on 2015/9/23.
 * Modified from volley
 */
public class WebImage extends LruImage {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    /**
     * Decoding lock so that we don't decode more than one image at a time (to avoid OOM's)
     */
    private static final Object sDecodeLock = new Object();
    private final Config mDecodeConfig;

    private final int mMaxWidth;
    private final int mMaxHeight;
    private final String url;
    private final ScaleType mScaleType;


    public WebImage(String url) {
        this(url, 0, 0, null, null);
    }

    public WebImage(String url, int maxWidth, int maxHeight,
                    ScaleType scaleType, Config decodeConfig) {
        this.url = url;
        mDecodeConfig = decodeConfig;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mScaleType = scaleType;
        setCacheLevel(CACHE_LEVEL_DISK_CACHE | CACHE_LEVEL_MEMORY_CACHE);
    }

    /**
     * 根据 ScaleType 和另一个维度是否有指定来计算目标尺寸
     * 1. 长和宽都没有指定的时候返回实际尺寸
     * 2. ScaleType.FIT_XY 如果有最大尺寸，返回最大尺寸，无则返回实际尺寸
     *    实际意义是将目标尺寸拉伸到实际尺寸
     * 3. 两个最大值都有一个未指定，如果是次要尺寸，则返回最大尺寸
     *    如果是主要尺寸，则根据次要尺寸的缩放率，通过实际尺寸计算出目标尺寸
     * 4. 两个值都有指定
     *    在保持实际长宽比的情况下，根据有无 ScaleType.CENTER_CROP
     *    有则，将实际矩形拉伸到目标矩形，可能会超出目标矩形
     *    无则，将实际矩形缩放到目标矩形，不会超出目标矩形
     *
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary      Maximum size of the primary dimension (i.e. width for
     *                        max width), or zero to maintain aspect ratio with secondary
     *                        dimension
     * @param maxSecondary    Maximum size of the secondary dimension, or zero to
     *                        maintain aspect ratio with primary dimension
     * @param actualPrimary   Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     * @param scaleType       The ScaleType used to calculate the needed image size.
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                           int actualSecondary, ScaleType scaleType) {

        // If no dominant value at all, just return the actual.
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ScaleType.FIT_XY) {
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            return maxPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect ratio.
        if (scaleType == ScaleType.CENTER_CROP) {
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth   Actual width of the bitmap
     * @param actualHeight  Actual height of the bitmap
     * @param desiredWidth  Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    /**
     * FIXME 缓存有个问题，应该缓存原始图片还是缩放后的图片？
     * @param context
     * @return
     * @throws LruImageException
     */
    @Override
    protected Bitmap loadBitmap(Context context) throws LruImageException {
        synchronized (sDecodeLock) {
            try {
                return doParse();
            } catch (OutOfMemoryError | IOException e) {
                throw new LruImageException(e);
            }
        }
    }

    private URLConnection newConnection() throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        return conn;
    }

    /**
     * load Bitmap from net to memory
     *
     * @return bytes of bitmap
     * @throws IOException
     */
    private byte[] loadData() throws IOException {

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
        return buffer.toByteArray();
    }

    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     */
    private Bitmap doParse() throws IOException {
        byte[] data = loadData();
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            decodeOptions.inPreferredConfig = mDecodeConfig;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight, mScaleType);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth, mScaleType);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
            // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
            decodeOptions.inSampleSize =
                    findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap =
                    BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                    tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap,
                        desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        return bitmap;
    }

    @Override
    public String getKey() {
        String s = "Volley:" + mMaxWidth + url + mMaxWidth;
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

        public Builder setMaxSize(int maxWidth,int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         *
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

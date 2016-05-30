package info.dourok.lruimage.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.IOException;

import info.dourok.lruimage.LruImage;
import info.dourok.lruimage.LruImageException;

/**
 * Created by larry on 5/30/16.
 */
public abstract class UrlImage extends LruImage {
    /**
     * Decoding lock so that we don't decode more than one image at a time (to avoid OOM's)
     */
    private static final Object sDecodeLock = new Object();
    private final Bitmap.Config mDecodeConfig;

    private final int mMaxWidth;
    private final int mMaxHeight;
    private final ImageView.ScaleType mScaleType;

    public UrlImage(int maxWidth, int maxHeight,
                    ImageView.ScaleType scaleType, Bitmap.Config decodeConfig) {
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
     * 实际意义是将目标尺寸拉伸到实际尺寸
     * 3. 两个最大值都有一个未指定，如果是次要尺寸，则返回最大尺寸
     * 如果是主要尺寸，则根据次要尺寸的缩放率，通过实际尺寸计算出目标尺寸
     * 4. 两个值都有指定
     * 在保持实际长宽比的情况下，根据有无 ScaleType.CENTER_CROP
     * 有则，将实际矩形拉伸到目标矩形，可能会超出目标矩形
     * 无则，将实际矩形缩放到目标矩形，不会超出目标矩形
     * <p>
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
                                           int actualSecondary, ImageView.ScaleType scaleType) {

        // If no dominant value at all, just return the actual.
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ImageView.ScaleType.FIT_XY) {
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
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
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
     * FIXME 硬盘缓存有个问题，应该缓存原始图片还是缩放后的图片？
     *
     * @param context
     * @return
     * @throws LruImageException
     */
    @Override
    protected final Bitmap loadBitmap(Context context) throws LruImageException {
        try {
            prepareData(context);
            synchronized (sDecodeLock) {
                return doParse(context);
            }
        } catch (IOException e) {
            throw new LruImageException(e);
        }
    }

    protected abstract void prepareData(Context context) throws IOException;

    protected abstract Bitmap decodingBitmap(Context context, BitmapFactory.Options decodeOptions) throws IOException;

    protected abstract void onDecodeFinish();

    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     *
     * @param context
     */
    private Bitmap doParse(Context context) throws IOException {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            decodeOptions.inPreferredConfig = mDecodeConfig;
            bitmap = decodingBitmap(context, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            decodingBitmap(context, decodeOptions);
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
            Bitmap tempBitmap = decodingBitmap(context, decodeOptions);

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

        onDecodeFinish();
        return bitmap;
    }


    @Override
    public String getKey() {
        return mMaxWidth + "&" + mMaxWidth + "&" + mScaleType;
    }

    public static class Builder {
        private Bitmap.Config decodeConfig;
        private int maxWidth;
        private int maxHeight;
        private String url;
        private ImageView.ScaleType scaleType;

        public Builder(String url) {
            this.url = url;
        }

        public Builder setDecodeConfig(Bitmap.Config decodeConfig) {
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
        public Builder setScaleType(ImageView.ScaleType scaleType) {
            this.scaleType = scaleType;
            return this;
        }

        public UrlImage create() {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return new WebImage(url, maxWidth, maxHeight, scaleType, decodeConfig);
            } else if (url.startsWith("content:")) {
                return new ContentImage(Uri.parse(url), maxWidth, maxHeight, scaleType, decodeConfig);
            } else if (url.startsWith("file://")) {
                return new FileImage(url, maxWidth, maxHeight, scaleType, decodeConfig);
            } else {
                return new FileImage(url, maxWidth, maxHeight, scaleType, decodeConfig);
            }

        }
    }
}

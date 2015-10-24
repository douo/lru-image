package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Created by John on 2015/10/24.
 * 支持 Progress
 * 会见整张图片的字节读取到内存中
 */
public class BufferWebImage extends WebImage {
    public BufferWebImage(String url) {
        this(url, NO_REQ_SIZE, NO_REQ_SIZE, false);
    }

    public BufferWebImage(String url, int cacheLevel) {
        this(url, NO_REQ_SIZE, NO_REQ_SIZE, false, cacheLevel);
    }

    public BufferWebImage(String url, int reqWidth, int reqHeight) {
        this(url, reqWidth, reqHeight, false);
    }

    public BufferWebImage(String url, int reqWidth, int reqHeight, boolean reqSize) {
        this(url, reqWidth, reqHeight, reqSize, CACHE_LEVEL_DISK_CACHE | CACHE_LEVEL_MEMORY_CACHE);
    }

    public BufferWebImage(String url, int reqWidth, int reqHeight, boolean reqSize, int cacheLevel) {
        super(url, reqWidth, reqHeight, reqSize, cacheLevel);
    }

    /**
     * @param context
     * @return Bitmap from source, or null if any ioexception or bitmapfactory can't decode
     */
    @Override
    protected Bitmap loadBitmap(Context context) throws LruImageException {
        Bitmap bitmap;
        final byte[] data;
        try {

            URLConnection conn = newConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            int length = conn.getContentLength();
            byte[] buffer = new byte[16384];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
            int sumPos = 0, pos;
            while (true) {
                pos = is.read(buffer);
                if (pos > 0) {
                    sumPos += pos;
                    baos.write(buffer, 0, pos);
                    progressUpdate(length, sumPos);
                } else {
                    break;
                }
            }
            data = baos.toByteArray();
            is.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new LruImageException(e);
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        if (reqWidth != NO_REQ_SIZE || reqHeight != NO_REQ_SIZE) {
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            Bitmap _bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
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
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }

}

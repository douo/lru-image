package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.IOException;

/**
 * 从文件中读取图片，支持 file 协议和绝对路径
 */
public class FileImage extends ScalableImage {
    private String filePath;

    public FileImage(String url, int maxWidth, int maxHeight,
                     ImageView.ScaleType scaleType, Bitmap.Config decodeConfig) {
        super(maxWidth, maxHeight, scaleType, decodeConfig);
        if (url.startsWith("file://")) {
            url = url.substring("file://".length());
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        filePath = url;
        setCacheLevel(CACHE_LEVEL_MEMORY_CACHE);
    }

    @Override
    protected void prepareData(Context context) throws IOException {

    }

    @Override
    protected Bitmap decodingBitmap(Context context, BitmapFactory.Options decodeOptions) {
        return BitmapFactory.decodeFile(filePath, decodeOptions);
    }

    @Override
    protected void onDecodeFinish() {

    }

    @Override
    public String getKey() {
        return Integer.toHexString((filePath + super.getKey()).hashCode());
    }
}

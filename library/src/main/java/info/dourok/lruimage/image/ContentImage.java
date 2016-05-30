package info.dourok.lruimage.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by larry on 5/30/16.
 */
public class ContentImage extends UrlImage {
    private Uri imageUri;

    public ContentImage(Uri imageUri, int maxWidth, int maxHeight,
                        ImageView.ScaleType scaleType, Bitmap.Config decodeConfig) {
        super(maxWidth, maxHeight, scaleType, decodeConfig);
        this.imageUri = imageUri;
        setCacheLevel(CACHE_LEVEL_MEMORY_CACHE);
    }

    @Override
    protected void prepareData(Context context) throws IOException {

    }

    @Override
    protected Bitmap decodingBitmap(Context context, BitmapFactory.Options decodeOptions) throws IOException {
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, decodeOptions);
    }

    @Override
    protected void onDecodeFinish() {

    }
}

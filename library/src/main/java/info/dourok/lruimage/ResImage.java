package info.dourok.lruimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by charry on 2014/11/21.
 */
public class ResImage extends LruImage{
    private  int resId;
    public ResImage(int resId){
        this.resId = resId;
    }
    @Override
    protected Bitmap loadBitmap(Context context) {
        return BitmapFactory.decodeResource(context.getResources(),resId);
    }

    @Override
    public String getKey() {
        return "ResImage"+resId;
    }
}

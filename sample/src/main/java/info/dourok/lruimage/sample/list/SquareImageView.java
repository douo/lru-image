package info.dourok.lruimage.sample.list;

import android.content.Context;
import android.util.AttributeSet;

import info.dourok.lruimage.LruImageView;

/**
 * Created by John on 2015/11/18.
 */
public class SquareImageView extends LruImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}

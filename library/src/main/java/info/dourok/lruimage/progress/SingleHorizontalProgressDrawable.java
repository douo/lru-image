package info.dourok.lruimage.progress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class SingleHorizontalProgressDrawable extends ProgressDrawableBase {

    private static final float PROGRESS_INTRINSIC_HEIGHT_DP = 3.2f;
    private static final RectF RECT_BOUND = new RectF(-180, -1, 180, 1);

    private int mProgressIntrinsicHeight;
    private int mProgressIntrinsicWidth;
    private float mTrackAlpha;

    public SingleHorizontalProgressDrawable(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        mProgressIntrinsicHeight = Math.round(PROGRESS_INTRINSIC_HEIGHT_DP * density);
        mProgressIntrinsicWidth = mProgressIntrinsicHeight * 12;
        mTrackAlpha = getFloatFromAttrRes(android.R.attr.disabledAlpha, context);
    }

    public static float getFloatFromAttrRes(int attrRes, Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{attrRes});
        try {
            return a.getFloat(0, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    public int getIntrinsicHeight() {

        return mProgressIntrinsicHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mProgressIntrinsicWidth;
    }


    protected boolean onLevelChange(int level) {
        invalidateSelf();
        return true;
    }

    protected void onPreparePaint(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas, int width, int height, Paint paint) {
        canvas.scale(width / RECT_BOUND.width(), height / RECT_BOUND.height());
        canvas.translate(RECT_BOUND.width() / 2, RECT_BOUND.height() / 2);

        paint.setAlpha(Math.round(mAlpha * mTrackAlpha));
        drawTrackRect(canvas, paint);
        paint.setAlpha(mAlpha);
        drawProgressRect(canvas, paint);
    }

    private static void drawTrackRect(Canvas canvas, Paint paint) {
        canvas.drawRect(RECT_BOUND, paint);
    }

    private void drawProgressRect(Canvas canvas, Paint paint) {

        int level = getLevel();
        if (level == 0) {
            return;
        }
        int saveCount = canvas.save();
        canvas.scale((float) level / LEVEL_MAX, 1, RECT_BOUND.left, 0);
        canvas.drawRect(RECT_BOUND, paint);
        canvas.restoreToCount(saveCount);
    }

}
package info.dourok.lruimage.sample.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by John on 2015/10/24.
 */
public class CircleProgressDrawable extends ProgressDrawableBase {
    private static final float PROGRESS_RADIOUS = 64f;
    private static final float PROGRESS_WIDTH = 2f;
    private static final int LEVEL_MAX = 10000;
    private int mIntriniscRadious;
    private int strokeWidth;
    private float mTrackAlpha;

    public CircleProgressDrawable(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        mIntriniscRadious = Math.round(PROGRESS_RADIOUS * density);
        strokeWidth = Math.round(PROGRESS_WIDTH * density);
        mTrackAlpha = ThemeUtils.getFloatFromAttrRes(android.R.attr.disabledAlpha, context);
    }


    @Override
    public int getIntrinsicHeight() {

        return mIntriniscRadious;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntriniscRadious;
    }

    @Override
    protected void onPreparePaint(Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected boolean onLevelChange(int level) {
        invalidateSelf();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas, int width, int height, Paint paint) {
        paint.setAlpha(Math.round(mAlpha * mTrackAlpha));
        drawTrackRect(canvas, paint);
        paint.setAlpha(mAlpha);
        drawProgressRect(canvas, paint);
    }

    private void drawTrackRect(Canvas canvas, Paint paint) {
        Rect bounds = getBounds();
        canvas.drawArc((float) 0, (float) 0, (float) bounds.width(), (float) bounds.height(), (float) 0, (float) 360, false, paint);
    }

    private void drawProgressRect(Canvas canvas, Paint paint) {
        Rect bounds = getBounds();
        int level = getLevel();
        if (level == 0) {
            return;
        }
        canvas.drawArc((float) 0, (float) 0, (float) bounds.width(), (float) bounds.height(), (float) 0, 360.f * (float) level / LEVEL_MAX, false, paint);

    }
}

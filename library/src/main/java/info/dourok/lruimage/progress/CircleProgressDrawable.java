package info.dourok.lruimage.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by John on 2015/10/24.
 */

public class CircleProgressDrawable extends ProgressDrawableBase {
    private static final float PROGRESS_RADIOUS = 64f;
    private static final float PROGRESS_WIDTH = 2f;
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

    RectF rectF = new RectF();

    private void drawTrackRect(Canvas canvas, Paint paint) {
        rectF.set(getBounds());
        canvas.drawArc(rectF, -90f, 360.f, false, paint);
    }

    private void drawProgressRect(Canvas canvas, Paint paint) {
        rectF.set(getBounds());
        int level = getLevel();
        if (level == 0) {
            return;
        }
        canvas.drawArc(rectF, -90f, 360.f * level / LEVEL_MAX, false, paint);

    }
}

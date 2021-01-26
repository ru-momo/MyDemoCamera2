package com.momo.camera2test.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * module  TW_APP_SnapdragonCamera
 * author  zhaoxuan
 * date  2018/11/21
 * description 自定义View，实现网格的九宫格和黄金比例显示
 */
public class CustomGridView extends View {
    private Paint mPaint;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private int viewStyle = 0;
    private int width, height;
    private int padding;
    private boolean state;


    public CustomGridView(Context context) {
        this(context, null);
    }

    public CustomGridView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void setStyle(int viewStyle) {
        this.viewStyle = viewStyle;
    }

    public void setState(boolean state){
        this.state = state;
        invalidate();
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (h / 55 * 34 < w) {
            padding = (w - h / 55 * 34) / 2;
        } else {
            padding = (h - w / 34 * 55) / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("CustomGridView","onDraw");
        super.onDraw(canvas);
        if (state){
            Log.e("CustomGridView","state");
            if (viewStyle == 1) {
                mPaint.setColor(Color.WHITE);
                mPaint.setStrokeWidth(1);
                for (int i = 0; i < 2; i++) {
                    canvas.drawLine(0, height / 3 * (i + 1), width, height / 3 * (i + 1), mPaint);
                    canvas.drawLine(width / 3 * (i + 1), 0, width / 3 * (i + 1), height, mPaint);
                }
            } else if (viewStyle == 2){
                mPaint.setColor(Color.parseColor("#50ffffff"));
                mPaint.setStrokeWidth(1);
                if (height / 55 * 34 < width) {
                    canvas.drawLine(padding, 0, padding, height, mPaint);
                    canvas.drawLine(padding, 1, width - padding, 1, mPaint);
                    canvas.drawLine(padding, height - 1, width - padding, height - 1, mPaint);
                    canvas.drawLine(width - padding, 0, width - padding, height, mPaint);

                    canvas.drawLine(padding, height / 55 * 13, padding + height / 55 * 13, height / 55 * 13, mPaint);
                    canvas.drawLine(padding, height / 55 * 21, width - padding, height / 55 * 21, mPaint);
                    canvas.drawLine(height / 55 * 8 + padding, height / 55 * 13, height / 55 * 8 + padding, height / 55 * 21, mPaint);
                    canvas.drawLine(padding + height / 55 * 13, 0, padding + height / 55 * 13, height / 55 * 21, mPaint);

                    mPaint.setColor(Color.WHITE);
                    mPaint.setStyle(Paint.Style.STROKE);
                    RectF rectF = new RectF(height / 55 * 8 + padding, height / 55 * 13, height / 55 * 12 + padding, height / 55 * 17);
                    canvas.drawArc(rectF, 180, 90, false, mPaint);
                    rectF = new RectF(height / 55 * 7 + padding, height / 55 * 13, height / 55 * 13 + padding, height / 55 * 19);
                    canvas.drawArc(rectF, -90, 90, false, mPaint);
                    rectF = new RectF(height / 55 * 3 + padding, height / 55 * 11, height / 55 * 13 + padding, height / 55 * 21);
                    canvas.drawArc(rectF, 0, 90, false, mPaint);
                    rectF = new RectF(padding, height / 55 * 5, height / 55 * 16 + padding, height / 55 * 21);
                    canvas.drawArc(rectF, 90, 90, false, mPaint);
                    rectF = new RectF(padding, 0, height / 55 * 26 + padding, height / 55 * 26);
                    canvas.drawArc(rectF, 180, 90, false, mPaint);
                    rectF = new RectF(width - padding - height / 55 * 42, 0, width - padding, height / 55 * 42);
                    canvas.drawArc(rectF, -90, 90, false, mPaint);
                    rectF = new RectF(width - padding - height / 55 * 68, height - height / 55 * 68, width - padding, height);
                    canvas.drawArc(rectF, -2, 92, false, mPaint);
                } else {
                    canvas.drawLine(1, padding, 1, height - padding, mPaint);
                    canvas.drawLine(0, padding, width, padding, mPaint);
                    canvas.drawLine(width - 1, padding, width - 1, height - padding, mPaint);
                    canvas.drawLine(0, height - padding, width, height - padding, mPaint);

                    canvas.drawLine(0, padding + width / 34 * 13, width / 34 * 13, padding + width / 34 * 13, mPaint);
                    canvas.drawLine(0, padding + width / 34 * 21, width, padding + width / 34 * 21, mPaint);
                    canvas.drawLine(width / 34 * 8, padding + width / 34 * 13, width / 34 * 8, padding + width / 34 * 21, mPaint);
                    canvas.drawLine(width / 34 * 13, padding, width / 34 * 13, padding + width / 34 * 21, mPaint);

                    mPaint.setColor(Color.WHITE);
                    mPaint.setStyle(Paint.Style.STROKE);
                    RectF rectF = new RectF(width / 34 * 8, padding + width / 34 * 13, width / 34 * 12, padding + width / 34 * 17);
                    canvas.drawArc(rectF, 180, 90, false, mPaint);
                    rectF = new RectF(width / 34 * 7, padding + width / 34 * 13, width / 34 * 13, padding + width / 34 * 19);
                    canvas.drawArc(rectF, -90, 90, false, mPaint);
                    rectF = new RectF(width / 34 * 3, padding + width / 34 * 11, width / 34 * 13, padding + width / 34 * 21);
                    canvas.drawArc(rectF, 0, 90, false, mPaint);
                    rectF = new RectF(0, padding + width / 34 * 5, width / 34 * 16, padding + width / 34 * 21);
                    canvas.drawArc(rectF, 90, 90, false, mPaint);
                    rectF = new RectF(0, padding, width / 34 * 26, padding + width / 34 * 26);
                    canvas.drawArc(rectF, 180, 90, false, mPaint);
                    rectF = new RectF(width - width / 34 * 42, padding, width, padding + width / 34 * 42);
                    canvas.drawArc(rectF, -90, 90, false, mPaint);
                    rectF = new RectF(width - width / 34 * 68, height - padding - width / 34 * 68, width, height - padding);
                    canvas.drawArc(rectF, -2, 92, false, mPaint);
                }
            }
        } else {
            Log.e("CustomGridView","else");
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            setLayerType(LAYER_TYPE_HARDWARE,null);
        }
    }
}

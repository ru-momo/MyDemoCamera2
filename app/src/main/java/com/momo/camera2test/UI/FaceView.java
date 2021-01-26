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
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FaceView extends View {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private Paint mPaint;
    private Context mContext;
    private int mCorlor = 0xff42ed45;
    private List<RectF> mFaces;
    private Canvas mCanvas;

    private String TAG = "FaceView";

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setColor(mCorlor);
        mPaint.setStyle(Paint.Style.STROKE);
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f,
                mContext.getResources().getDisplayMetrics());
        mPaint.setStrokeWidth(v);
        boolean antiAlias = mPaint.isAntiAlias();
        Log.e(TAG, "antiAlias = "+antiAlias);
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
        Log.e("FaceView","onMeasure");
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        if (mFaces != null) {
            for (RectF mFace : mFaces) {         //因为会同时存在多张人脸，所以用循环
                canvas.drawRect(mFace, mPaint);     //绘制人脸所在位置的矩形
            }
        }
    }

    public void setFaces(ArrayList<RectF> faces){       //设置人脸信息，然后刷新FaceView
        this.mFaces = faces;
        invalidate();
    }

}

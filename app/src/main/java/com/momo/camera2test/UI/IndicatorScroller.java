package com.momo.camera2test.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import com.momo.camera2test.Camera2Fragment;
import com.momo.camera2test.Utils.IndicatorUtils;

public class IndicatorScroller extends ViewGroup {

    private Scroller mScroller;
    private static boolean mIsLayoutView = false;

    public Scroller getmScroller() {
        return mScroller;
    }

    private int mLeftIndex;
    private int mRightIndex;

    public void setmLeftIndex(int mLeftIndex) {
        this.mLeftIndex = mLeftIndex;
    }

    public int getmLeftIndex() {
        return mLeftIndex;
    }

    public void setmRightIndex(int mRightIndex) {
        this.mRightIndex = mRightIndex;
    }

    public int getmRightIndex() {
        return mRightIndex;
    }

    private int mDuration = 1000;

    public int getmDuration() {
        return mDuration;
    }

    public IndicatorScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        mIsLayoutView = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mIsLayoutView) {
            return;
        }
        mIsLayoutView = true;
        int cCount = getChildCount();
        int childLeft = 0;
        int childRight = 0;
        int selectedMode = IndicatorUtils.getCurrentSelectedIndex();
        int widthOffset = 0;        //居中显示
        /**
         * 遍历所有childView根据其宽和高，不考虑margin
         */
        for (int i = 0; i < cCount; i++) {
            int fi = i;
            View childView = getChildAt(i);
            if (i < selectedMode) {
                widthOffset += childView.getMeasuredWidth();
            }
        }

        for (int i = 0; i < cCount; i++) {
            View childView = getChildAt(i);
            if (i != 0) {
                View preView = getChildAt(i - 1);
                childLeft = preView.getRight();
                childRight = childLeft + childView.getMeasuredWidth();
            } else {
                childLeft = (getWidth() - getChildAt(selectedMode).getMeasuredWidth()) / 2 - widthOffset;
                childRight = childLeft + childView.getMeasuredWidth();
            }
            childView.layout(childLeft, t, childRight, b);
        }

        TextView indexText = (TextView) getChildAt(selectedMode);
        indexText.setTextColor(0xff1996ff);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //测量所有子元素
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //处理wrap_content的情况
        if (getChildCount() == 0) {
            setMeasuredDimension(0, 0);
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            View childOne = getChildAt(0);
            int childWidth = childOne.getMeasuredWidth();
            int childHeight = childOne.getMeasuredHeight();
            setMeasuredDimension(childWidth * getChildCount(), childHeight);
        } else if (widthMode == MeasureSpec.AT_MOST){
            View childOne = getChildAt(0);
            int childWidth = childOne.getMeasuredWidth();
            setMeasuredDimension(childWidth * getChildCount(), heightSize);
        }else if (heightMode == MeasureSpec.AT_MOST){
            int childHeight = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(widthSize, childHeight);
        }
        //如果自定义ViewGroup之初就已确定宽高都是match_parent, 那么直接设置即可
//        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * 重写computeScroll()实现过渡滑动；
     */
    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            //滑动未结束，内部使用scrollTo方法完成实际滑动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }else {
            //滑动结束，执行对应操作， 建议利用接口进行监听
            Log.e("IndicatorScroller","computeScroll滑动结束，执行对应操作");
        }
        super.computeScroll();
    }

    public final void scrollToNext(int preIndex, int nextIndex) {
        TextView selectedText = (TextView) getChildAt(preIndex);
        if (selectedText != null) {
            selectedText.setTextColor(0xff000000);
        }
        selectedText = (TextView) getChildAt(nextIndex);
        if (selectedText != null) {
            selectedText.setTextColor(0xff1996ff);
        }
    }

}

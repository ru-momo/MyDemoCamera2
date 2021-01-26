package com.momo.camera2test.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.momo.camera2test.CameraUI;
import com.momo.camera2test.R;
import com.momo.camera2test.Utils.IndicatorUtils;

import static androidx.customview.widget.ViewDragHelper.INVALID_POINTER;


public class IndicatorView extends LinearLayout {

    private IndicatorScroller mIndicatorScroller;
    private Context mContext;
    private int mSelectedIndex = IndicatorUtils.getCurrentSelectedIndex();
    private int mTouchSlop;
    private int mActivePointerId = INVALID_POINTER;
    private CameraUI mCameraUI;

    public int getmSelectedIndex() {
        return mSelectedIndex;
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.indicator_scroller_layout, this, true);
    }

    public void init(){
        mIndicatorScroller = findViewById(R.id.canera_scroller);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mCameraUI = CameraUI.getCameraUI();

        int childCount = mIndicatorScroller.getChildCount();
        for (int i = 0; i < childCount; i++) {
            int fi = i;
            View childAt = mIndicatorScroller.getChildAt(i);
            childAt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("IndicatorView",""+ fi);
                    moveToPoint(fi);
                    mCameraUI.setModule(fi);
                }
            });
        }
    }

    public void moveLeft() {
        mIndicatorScroller.setmLeftIndex(IndicatorUtils.getCurrentSelectedIndex() - 1);
        mIndicatorScroller.setmRightIndex(IndicatorUtils.getCurrentSelectedIndex());
        IndicatorUtils.sIsClickerIndicator = false;
        int k = Math.round((mIndicatorScroller.getChildAt(mIndicatorScroller.getmLeftIndex()).getWidth() + mIndicatorScroller.getChildAt(mIndicatorScroller.getmRightIndex()).getWidth()) / 2.0F);
        mIndicatorScroller.getmScroller().startScroll(mIndicatorScroller.getScrollX(), 0, -k, 0, mIndicatorScroller.getmDuration());
        mIndicatorScroller.scrollToNext(mIndicatorScroller.getmRightIndex(), mIndicatorScroller.getmLeftIndex());
        IndicatorUtils.setSelectedIndex(IndicatorUtils.getCurrentSelectedIndex() - 1);
        mIndicatorScroller.invalidate();
        mSelectedIndex --;
    }

    public void moveRight() {
        mIndicatorScroller.setmLeftIndex(IndicatorUtils.getCurrentSelectedIndex());
        mIndicatorScroller.setmRightIndex(IndicatorUtils.getCurrentSelectedIndex() + 1);
        IndicatorUtils.sIsClickerIndicator = false;
        int k = Math.round((mIndicatorScroller.getChildAt(mIndicatorScroller.getmLeftIndex()).getWidth() + mIndicatorScroller.getChildAt(mIndicatorScroller.getmRightIndex()).getWidth()) / 2.0F);
        mIndicatorScroller.getmScroller().startScroll(mIndicatorScroller.getScrollX(), 0, k, 0, mIndicatorScroller.getmDuration());
        mIndicatorScroller.scrollToNext(mIndicatorScroller.getmLeftIndex(), mIndicatorScroller.getmRightIndex());
        IndicatorUtils.setSelectedIndex(IndicatorUtils.getCurrentSelectedIndex() + 1);
        mIndicatorScroller.invalidate();
        mSelectedIndex ++;
    }

    public void moveToPoint(int index){
        mIndicatorScroller.setmLeftIndex(index);
        mIndicatorScroller.setmRightIndex(index + 1);
        IndicatorUtils.sIsClickerIndicator = false;

        int count = mSelectedIndex - mIndicatorScroller.getmLeftIndex();
        int mWidth = mIndicatorScroller.getChildAt(mSelectedIndex).getWidth();
        int j = Math.round((count * mWidth));
        mIndicatorScroller.getmScroller().startScroll(mIndicatorScroller.getScrollX(), 0, -j, 0, mIndicatorScroller.getmDuration());
        mIndicatorScroller.scrollToNext(mSelectedIndex, mIndicatorScroller.getmLeftIndex());
        IndicatorUtils.setSelectedIndex(index);
        mIndicatorScroller.invalidate();
        mSelectedIndex = index;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return scrollEvent(ev) || super.dispatchTouchEvent(ev);
    }


    private int mLastMotionX;
    private int mLastMotionY;
    private boolean mIS;
    private boolean scrollEvent(MotionEvent ev){
        boolean isDoAction = false;
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                isDoAction = false;
                mIS = false;
                return !super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }
                if (mIS){
                    break;
                }

                final int x = (int) ev.getX(activePointerIndex);
                final int y = (int) ev.getY(activePointerIndex);
                int deltaX = mLastMotionX - x;
                int deltaY = mLastMotionY - y;
                int absDeltaX = Math.abs(deltaX);
                int absDeltaY = Math.abs(deltaY);

                if (!isDoAction && absDeltaX > mTouchSlop && absDeltaX > absDeltaY) {
                    isDoAction = true;
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    if (deltaX > 100 && mSelectedIndex < IndicatorUtils.MAX_INDEX){
                        moveRight();
                        mCameraUI.setModule(mSelectedIndex);
                        Log.e("IndicatorView","moveRight();"+ mSelectedIndex);
                        mIS = true;
                    } else if (deltaX < -100 && mSelectedIndex > IndicatorUtils.MIN_INDEX){
                        moveLeft();
                        mCameraUI.setModule(mSelectedIndex);
                        Log.e("IndicatorView","moveLeft();" + mSelectedIndex);
                        mIS = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mIS = false;
                break;
        }
        return isDoAction;
    }


}

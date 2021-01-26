package com.momo.camera2test.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.momo.camera2test.Camera2Fragment;
import com.momo.camera2test.CameraUI;

import java.lang.ref.WeakReference;

public class Cicle extends View {

    private String TAG = "Cicle";
    private Paint mPaint;
    private int STATE = STATE_PICTURE;          //当前模式
    public static final int STATE_PICTURE = 0; //拍照
    public static final int STATE_VIDEO = 1;   //录像
    private boolean isRecording = false;    //是否在录像
    public static boolean isContinuityPicture = false;    //是否连拍

    private int ACTION = 0;

    private Handler mHandler = new Myhandler();
    private Message msg;

    public Cicle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mPaint = new Paint();
    }

    public void setSTATE(int state){
        STATE = state;
        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "onDraw ACTION =" + ACTION);
        if (STATE == STATE_PICTURE){
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(14);
            mPaint.setStyle(Paint.Style.STROKE);
            if (ACTION == 0){
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, 75, mPaint);
            }else {
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, 65, mPaint);
            }

        }else if (STATE == STATE_VIDEO){
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(14);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 75, mPaint);
            if (!isRecording){
                mPaint.setColor(Color.RED);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, 65, mPaint);
            }else {
                mPaint.setColor(Color.RED);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(getWidth() / 2 - 40, getHeight() / 2 - 40,
                        getWidth() / 2 + 40, getHeight() / 2 + 40, mPaint);
            }
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean isDoAction = false;
        final int action = event.getAction();
        Log.e(TAG, " action = " + action);
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if (STATE == STATE_PICTURE){
                    msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    if (CameraUI.getCameraUI().getmCamera2Fragment().getmTimerState() == Camera2Fragment.TIMER_FIVE_SECONDS ||
                            CameraUI.getCameraUI().getmCamera2Fragment().getmTimerState() == Camera2Fragment.TIMER_TEN_SECONDS) {
                        isDoAction = true;
                        break;
                    }
                    msg = new Message();
                    msg.what = 3;
                    isContinuityPicture = true;
                    mHandler.sendMessageDelayed(msg, 1000);
                }else if (STATE == STATE_VIDEO){
                    msg = new Message();
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                }
                isDoAction = true;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isContinuityPicture = false;
                if (STATE == STATE_PICTURE){
                    msg = new Message();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
//                isDoAction = true;
                break;
        }
        return isDoAction;
    }

    private class Myhandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.e(TAG, " handleMessage msg.what = " +msg.what);
            switch (msg.what){
                case 0:
                    CameraUI.getCameraUI().getmCamera2Fragment().setStartContinuityPicture(false);
                    CameraUI.getCameraUI().getmCamera2Fragment().setContinuityPicture(false);
                    ACTION = 0;
                    invalidate();
                    break;

                case 1:
                    CameraUI.getCameraUI().takePicture();
                    ACTION = 1;
                    invalidate();
                    break;

                case 2:
                    if (!isRecording){
                        isRecording = true;
                        invalidate();
                    }else {
                        isRecording = false;
                        invalidate();
                    }
                    CameraUI.getCameraUI().takePicture();
                    break;

                case 3:
                    Log.e(TAG, " isContinuityPicture = " +isContinuityPicture);
                    if (isContinuityPicture){
                        CameraUI.getCameraUI().getmCamera2Fragment().setStartContinuityPicture(true);
//                        CameraUI.getCameraUI().takePicture();
                    }else {
                        CameraUI.getCameraUI().getmCamera2Fragment().setContinuityPicture(false);
                        Log.e(TAG, " 当前不是连续拍照！ " );
                    }
                    break;
            }
        }
    }
}

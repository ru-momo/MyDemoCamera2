package com.momo.camera2test.UI;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.momo.camera2test.Camera2Fragment;
import com.momo.camera2test.CameraUI;
import com.momo.camera2test.R;

public class TimerView extends androidx.appcompat.widget.AppCompatTextView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private float TEXT_SIZE_MAX = 200f;
    private float TEXT_SIZE_MIX = 10f;

    private int count = 0;
    private Context mContext;
    private TextView mTextView;
    private Handler mHandler = new MyHandler();
    private boolean r = true;

    private Animation mAnimation;
    private SoundPool mSoundPool;
    private int mBeepOnce;

    public TimerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
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

    private void init(){
        mTextView = this;
        mTextView.setTextSize(TEXT_SIZE_MAX);
        initSoundPool();
    }

    private void initSoundPool(){
        if (mSoundPool == null){
            mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
            mBeepOnce = mSoundPool.load(mContext, R.raw.beep_once, 1);
        }
    }

    public void releaseSoundPool() {
        if (mSoundPool != null) {
            mSoundPool.unload(R.raw.beep_once);
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    public void startCountDown(int residue){
        r = true;
        count = residue;
        mAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setDuration(800);
        if (mSoundPool == null){
            initSoundPool();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (r){
                    Log.e("TimerView","startCountDown  run()");
                    Message msg = new Message();
                    msg.what = 1;
                    if (count == 0){
                        r = false;
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                        break;
                    }else {
                        mHandler.sendMessage(msg);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        Thread thread = null;

        if (residue == 0){
            mTextView.setVisibility(GONE);
            return;
        }else if (residue > 0){
            mTextView.setVisibility(VISIBLE);
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.e("TimerView","msg.what = " + msg.what);
            switch (msg.what){
                case 1:
                    Log.e("TimerView","count = " + count);
                    mSoundPool.play(mBeepOnce, 1.0f, 1.0f, 0, 0, 1.0f);
                    mTextView.setText(String.valueOf(count));
                    count --;
                    mTextView.startAnimation(mAnimation);
                    break;
                case 2:
                    Log.e("TimerView","case = 2");
                    CameraUI.getCameraUI().timerTakePicture();
                    mTextView.setText("");
                    mTextView.clearAnimation();
                    count = 0;
                    break;
            }
        }
    }

}

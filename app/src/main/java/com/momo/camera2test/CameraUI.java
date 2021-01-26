package com.momo.camera2test;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.momo.camera2test.UI.Cicle;
import com.momo.camera2test.UI.CustomGridView;
import com.momo.camera2test.UI.FaceView;
import com.momo.camera2test.UI.IndicatorView;
import com.momo.camera2test.UI.TimerView;
import com.momo.camera2test.Utils.IndicatorUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class CameraUI implements View.OnClickListener{

    private Activity mActivity;
    private Camera2Fragment mCamera2Fragment;
    public Camera2Fragment getmCamera2Fragment() {
        return mCamera2Fragment;
    }

    private Context mContext;
    private static String TAG = "CameraUI";

    private int mIndex = IndicatorUtils.getCurrentSelectedIndex();     //当前模块
    private static final int VIDEO_MODULE_INDEX = 0;    //录像
    private static final int PHOTO_MODULE_INDEX = 1;    //拍照
    private static final int PORTRAIT_MODULE_INDEX = 2;    //人像

    private TextView mPhotoSize;                //拍照比例
    private TextView mVideoSize;                //录像比例
    private Cicle mPicture;                //拍照按钮
    private ImageButton mExchange;                //切换相机
    private ImageButton mLOOK;                     //水印
    private ImageButton mGrid;                 //网格开关
    private ImageButton mFlash;                 //闪光灯开关
    private ImageButton mTime;                 //定时器开关
    private CustomGridView mCustomGridView;      //网格线
    private IndicatorView mIndicatorView;        //功能滑动条
    private FaceView mFaceView;                 //人脸检测框
    private ImageView mImageView;               //缩略图
    private TimerView mTimerView;               //定时器显示

    public TimerView getmTimerView() {
        return mTimerView;
    }

    public ImageView getmImageView() {
        return mImageView;
    }

    public FaceView getmFaceView() {
        return mFaceView;
    }

    public int getmIndex() {
        return mIndex;
    }

    private static CameraUI cameraUI = null;

    public static CameraUI getCameraUI() {
        if (cameraUI == null){
            cameraUI = new CameraUI();
        }
        return cameraUI;
    }

    public CameraUI(){
    }

    public CameraUI(Camera2Fragment fragment) {
        cameraUI = this;
        mCamera2Fragment = fragment;
        mActivity = fragment.getActivity();
        mContext = fragment.getContext();
        init();
        Log.e(TAG, "getImagePath : "+getImagePath());
        setImageToView();
    }

    public void init(){
        mPhotoSize = mActivity.findViewById(R.id.photo_size);
        mVideoSize = mActivity.findViewById(R.id.video_size);
        mPicture = mActivity.findViewById(R.id.picture);
        mExchange = mActivity.findViewById(R.id.exchange);
        mCustomGridView = mActivity.findViewById(R.id.customGridView);
        mIndicatorView = mActivity.findViewById(R.id.indicatorView);
        mLOOK = mActivity.findViewById(R.id.LOOK);
        mGrid = mActivity.findViewById(R.id.GridBtn);
        mFlash = mActivity.findViewById(R.id.flash);
        mTime = mActivity.findViewById(R.id.time_clock);
        mFaceView = mActivity.findViewById(R.id.faceView);
        mImageView = mActivity.findViewById(R.id.thumbnail);
        mTimerView = mActivity.findViewById(R.id.timer);

        mPhotoSize.setOnClickListener(this);
        mVideoSize.setOnClickListener(this);
        mExchange.setOnClickListener(this);
        mLOOK.setOnClickListener(this);
        mGrid.setOnClickListener(this);
        mFlash.setOnClickListener(this);
        mTime.setOnClickListener(this);
        mImageView.setOnClickListener(this);
    }

    public void setModule(int indexModule){
        mIndex = indexModule;
        mCamera2Fragment.setmIndex(indexModule);
        Log.e("UI","setModule" + indexModule);
        switch (indexModule){
            case VIDEO_MODULE_INDEX:
                mPicture.setSTATE(Cicle.STATE_VIDEO);
                mPhotoSize.setVisibility(View.GONE);
                mVideoSize.setVisibility(View.VISIBLE);
                mLOOK.setVisibility(View.GONE);
                mFlash.setVisibility(View.GONE);
                mTime.setVisibility(View.GONE);
                break;
            case PHOTO_MODULE_INDEX:
                mPicture.setSTATE(Cicle.STATE_PICTURE);
                mPhotoSize.setVisibility(View.VISIBLE);
                mVideoSize.setVisibility(View.GONE);
                mExchange.setVisibility(View.VISIBLE);
                mLOOK.setVisibility(View.VISIBLE);
                mFlash.setVisibility(View.VISIBLE);
                mTime.setVisibility(View.VISIBLE);
                break;
            case PORTRAIT_MODULE_INDEX:
                break;
        }
        mCamera2Fragment.setModule();
    }


    @Override
    public void onClick(View v) {
        Log.e("UI","onClick" + v.getId());
        switch (v.getId()) {
            case R.id.exchange:
                if (mCamera2Fragment.getOpenCameraId().equals(Camera2Fragment.Camera0)){
                    mCamera2Fragment.setOpenCameraId(Camera2Fragment.Camera1);
                }else {
                    mCamera2Fragment.setOpenCameraId(Camera2Fragment.Camera0);
                }
                mCamera2Fragment.setModule();
                break;

            case R.id.photo_size:
                mCamera2Fragment.showSingleAlertDialog(Camera2Fragment.PHOTOSIZE_ONCLICK);
                break;

            case R.id.video_size:
                mCamera2Fragment.showSingleAlertDialog(Camera2Fragment.VIDEOSIZE_ONCLICK);
                break;

            case R.id.LOOK:
                mCamera2Fragment.showSingleAlertDialog(Camera2Fragment.LOOK_ONCLICK);
                break;

            case R.id.GridBtn:
                mCamera2Fragment.showSingleAlertDialog(Camera2Fragment.GRID_ONCLICK);
                break;

            case R.id.flash:
                mCamera2Fragment.showSingleAlertDialog(Camera2Fragment.FLASHLIGHT_ONCLICK);
                break;

            case R.id.time_clock:
                mCamera2Fragment.showSingleAlertDialog(Camera2Fragment.TIMECLOCK_ONCLICK);
                break;

            case R.id.thumbnail:
                File file = null;
                if (mCamera2Fragment.getImagePath() != null){
                    file = new File(mCamera2Fragment.getImagePath());
                }else {
                    file = new File(getImagePath());
                }
//                Uri uri = FileProvider.getUriForFile(mActivity.getApplicationContext(), "com.momo.camera2test.provider", file);
                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(uri, "image/*");
                mActivity.startActivity(intent);
                break;
        }
    }

    public void updateUI(int onClick){
        switch (onClick){
            case Camera2Fragment.PHOTOSIZE_ONCLICK:
                String openSize = mCamera2Fragment.getOpenSize();
                if (openSize.equals(Camera2Fragment.PHOTO_SIZE_ONE)){
                    mPhotoSize.setText(Camera2Fragment.PHOTO_SIZE_ONE);
                }else if (openSize.equals(Camera2Fragment.PHOTO_SIZE_TWO)){
                    mPhotoSize.setText(Camera2Fragment.PHOTO_SIZE_TWO);
                }else if (openSize.equals(Camera2Fragment.PHOTO_SIZE_THREE)){
                    mPhotoSize.setText(Camera2Fragment.PHOTO_SIZE_THREE);
                }
                break;

            case Camera2Fragment.FLASHLIGHT_ONCLICK:
                int flashState = mCamera2Fragment.getmFlashState();
                if (flashState == Camera2Fragment.FLASH_STATE_FASLE){
                    mFlash.setBackgroundResource(R.mipmap.flash_false);
                }else if (flashState == Camera2Fragment.FLASH_STATE_TRUE){
                    mFlash.setBackgroundResource(R.mipmap.flash_true);
                }else if (flashState == Camera2Fragment.FLASH_STATE_AUTO){
                    mFlash.setBackgroundResource(R.mipmap.flash_auto);
                }
                break;

            case Camera2Fragment.GRID_ONCLICK:
                int gridType = mCamera2Fragment.getGridType();
                if (gridType == Camera2Fragment.GRID_TYPE1 || gridType == Camera2Fragment.GRID_TYPE2){
                    mGrid.setBackgroundResource(R.mipmap.grid_true);
                }else if (gridType == Camera2Fragment.GRID_TYPE0){
                    mGrid.setBackgroundResource(R.mipmap.grid_false);
                }
                break;

            case Camera2Fragment.LOOK_ONCLICK:
                int openLOOK = mCamera2Fragment.getOpenLOOK();
                if (openLOOK == Camera2Fragment.OPENLOOK_FALSE) {
                    mLOOK.setBackgroundResource(R.mipmap.look_false);
                }else if (openLOOK == Camera2Fragment.OPENLOOK_TRUE){
                    mLOOK.setBackgroundResource(R.mipmap.look_true);
                }
                break;

            case Camera2Fragment.VIDEOSIZE_ONCLICK:
                int openVideoSize = mCamera2Fragment.getOpenVideoSize();
                if (openVideoSize == Camera2Fragment.VIDEO_SIZE_HIGHFPS){
                    mVideoSize.setText(Camera2Fragment.VIDEO_HIGHFPS);
                }else if (openVideoSize == Camera2Fragment.VIDEO_SIZE_LOWFPS){
                    mVideoSize.setText(Camera2Fragment.VIDEO_LOWFPS);
                }
                break;

            case Camera2Fragment.TIMECLOCK_ONCLICK:
                int timerState = mCamera2Fragment.getmTimerState();
                if (timerState == Camera2Fragment.TIMER_OFF){
                    mTime.setBackgroundResource(R.mipmap.time_false);
                }else if (timerState == Camera2Fragment.TIMER_FIVE_SECONDS){
                    mTime.setBackgroundResource(R.mipmap.time_five);
                }else if (timerState == Camera2Fragment.TIMER_TEN_SECONDS){
                    mTime.setBackgroundResource(R.mipmap.time_ten);
                }
                break;
        }
    }

    private void setImageToView(){
        Log.e(TAG, "setImageToView");
        String path = getImagePath();
        try {
            File file = new File(path);
            InputStream input = new FileInputStream(file);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(input);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 500, 500);
            mImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getImagePath(){
        String path = null;
        ContentResolver resolver = mActivity.getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                "MAX("+MediaStore.Images.Media.DATE_ADDED+")"};
        Cursor query = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        query.moveToFirst();
        while (!query.isAfterLast()){
//            Log.e(TAG, query.getString(query.getColumnIndex(MediaStore.Images.Media.DATA)));
            path = query.getString(query.getColumnIndex(MediaStore.Images.Media.DATA));
            query.moveToNext();
        }
        query.close();
        return path;
    }

    public void timerTakePicture(){
        mTimerView.setVisibility(View.GONE);
        mCamera2Fragment.takePicture();
    }

    public void takePicture(){
        if (mIndex == VIDEO_MODULE_INDEX) {
            if (mCamera2Fragment.isRunVideo) {
                mIndicatorView.setVisibility(View.VISIBLE);
                mVideoSize.setVisibility(View.VISIBLE);
                mGrid.setVisibility(View.VISIBLE);
                mExchange.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.VISIBLE);
            }else {
                mIndicatorView.setVisibility(View.GONE);
                mVideoSize.setVisibility(View.GONE);
                mGrid.setVisibility(View.GONE);
                mExchange.setVisibility(View.GONE);
                mImageView.setVisibility(View.GONE);
            }
            mCamera2Fragment.takePicture();
        }else if (mIndex == PHOTO_MODULE_INDEX){
            if (mCamera2Fragment.getmTimerState() == Camera2Fragment.TIMER_OFF){
                Log.e(TAG, "Cicle.isContinuityPicture = " + Cicle.isContinuityPicture);
                if (Cicle.isContinuityPicture){
                    mCamera2Fragment.setContinuityPicture(Cicle.isContinuityPicture);
                }else {
                    mCamera2Fragment.setContinuityPicture(!Cicle.isContinuityPicture);
                }
                mCamera2Fragment.takePicture();
            }else if (mCamera2Fragment.getmTimerState() == Camera2Fragment.TIMER_FIVE_SECONDS){
                mTimerView.startCountDown(5);
            }else if (mCamera2Fragment.getmTimerState() == Camera2Fragment.TIMER_TEN_SECONDS){
                mTimerView.startCountDown(10);
            }
        }else if (mIndex == PORTRAIT_MODULE_INDEX){
        }
    }
}

package com.momo.camera2test;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.momo.camera2test.UI.CustomGridView;
import com.momo.camera2test.UI.IndicatorView;
import com.momo.camera2test.UI.TimerView;
import com.momo.camera2test.Utils.IndicatorUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2Fragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private String openCameraId = Camera0;    //需要打开相机的id

    public String getOpenCameraId() {
        return openCameraId;
    }

    public void setOpenCameraId(String openCameraId) {
        this.openCameraId = openCameraId;
    }

    public static final String Camera0 = "0";
    public static final String Camera1 = "1";

    private String openSize = PHOTO_SIZE_ONE;        //需要设置的尺寸
    public String getOpenSize() {
        return openSize;
    }

    public static final String PHOTO_SIZE_ONE = "1:1";
    public static final String PHOTO_SIZE_TWO = "4:3";
    public static final String PHOTO_SIZE_THREE = "16:9";

    private int openVideoSize = VIDEO_SIZE_HIGHFPS;          //需要设置录像的格式
    public int getOpenVideoSize() {
        return openVideoSize;
    }
    public static final int VIDEO_SIZE_HIGHFPS = 0;     //60FPS
    public static final int VIDEO_SIZE_LOWFPS = 1;      //30FPS
    public static final String VIDEO_HIGHFPS = "1080P 60FPS";
    public static final String VIDEO_LOWFPS = "1080P 30FPS";

    private int openLOOK = OPENLOOK_FALSE;         //是否打开水印

    public int getOpenLOOK() {
        return openLOOK;
    }

    public static final int OPENLOOK_FALSE = 0;     //关闭水印
    public static final int OPENLOOK_TRUE = 1;    //打开水印

    private String mNextVideoAbsolutePath;  //录像视频保存路径

    private OrientationEventListener mOrientationListener;      //屏幕旋转角度监听
    private int mSaveImageOrientation;      //保存照片的方向

    public boolean isRunVideo = false;             //录像状态

    private CustomGridView mCustomGridView;           //网格线
    private int gridType = GRID_TYPE0;                //网格线类型

    public int getGridType() {
        return gridType;
    }

    public static final int GRID_TYPE0 = 0;
    public static final int GRID_TYPE1 = 1;
    public static final int GRID_TYPE2 = 2;

    private int mFlashState = FLASH_STATE_FASLE;        //闪光灯状态

    public int getmFlashState() {
        return mFlashState;
    }

    public static final int FLASH_STATE_FASLE = 0;  //关闭闪光灯
    public static final int FLASH_STATE_TRUE = 1;   //打开闪光灯
    public static final int FLASH_STATE_AUTO = 2;   //自动闪光灯

    private View mView;                    //

    private IndicatorView mIndicatorView;

    private CameraUI mCameraUI;

    private int mIndex = PHOTO_MODULE_INDEX;         //模式索引
    private boolean isPreview;  //是否在预览

    public void setmIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    private static final int VIDEO_MODULE_INDEX = 0;    //录像
    private static final int PHOTO_MODULE_INDEX = 1;    //拍照
    private static final int PORTRAIT_MODULE_INDEX = 2;    //人像

    private int onClickId;      //点击事件标识器
    public static final int PHOTOSIZE_ONCLICK = 0;     //拍照分辨率
    public static final int FLASHLIGHT_ONCLICK = 1;    //闪光灯
    public static final int GRID_ONCLICK = 2;          //网格
    public static final int LOOK_ONCLICK = 3;          //水印
    public static final int VIDEOSIZE_ONCLICK = 4;     //视频分辨率
    public static final int TIMECLOCK_ONCLICK = 5;      //定时器

    private String imagePath = null;            //图片路径
    public String getImagePath() {
        return imagePath;
    }

    private int mTimerState = TIMER_OFF;            //定时器状态
    public static final int TIMER_OFF = 0;          //关闭定时器
    public static final int TIMER_FIVE_SECONDS = 1; //五秒
    public static final int TIMER_TEN_SECONDS = 2;  //十秒
    public int getmTimerState() {
        return mTimerState;
    }

    private SoundPool mSoundPool;                   //声音池
    private int mBeepPicture;                              //声音资源
    private int mBeepVideo;                              //声音资源

    private boolean isUpdateThumbnail = true;           //是否更新缩略图

    /**
     * 录像的预览请求构造器
     */
    private CaptureRequest.Builder mVideoPreviewBuilder;

    /**
     * 权限申请
     */
    public static String[] permissionsREAD = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2Fragment";

    /**
     * ID of the current {@link CameraDevice}.正在使用的相机id
     */
    private String mCameraId = "0";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;//预览状态

    /**
     * Camera state: Waiting for the focus to be locked.等待自动对焦的焦点被锁状态
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.等待曝光为预捕获状态
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.等待曝光不是预捕获状态
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.	拍照状态，APP开始获取图片数据流进行保存
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * The current state of camera state for taking pictures.用于拍照的相机状态的当前状态
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;//默认状态为预览状态

    /**
     * An {@link AutoFitTextureView} for camera preview.预览使用的自定义TextureView控件
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.预览用的获取会话
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.正在使用的相机
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link Size} of camera preview.预览数据的尺寸
     */
    private Size mPreviewSize;

    /**
     * An additional thread for running tasks that shouldn't block the UI.处理拍照等工作的子线程
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * {@link CaptureRequest.Builder} for the camera preview预览请求构建器, 用来构建"预览请求"(下面定义的)通过pipeline发送到Camera device
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}预览请求, 由上面的构建器构建出来
     */
    private CaptureRequest mPreviewRequest;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.信号量控制器, 防止相机没有关闭时退出本应用
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.当前摄像头设备是否支持Flash
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    private Context mContext;

    private static String headPath = Environment.getExternalStorageDirectory() + File.separator +
            Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator;

    public static Camera2Fragment newInstance() {
        return new Camera2Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCameraPermission();//请求权限
        mOrientationListener = new OrientationEventListener(getContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
//                Log.e(TAG,""+orientation);
                mSaveImageOrientation = orientation;
//                Log.e(TAG,"mSaveImageOrientation"+mSaveImageOrientation);
            }
        };
    }

    public int getOrientationSe(int orientation) {
        if (orientation >= 300 || orientation < 50) { //0度  90 正竖屏
            return 0;
        } else if (orientation >= 50 && orientation < 110) { //90度 右横屏
            return 1;
        } else if (orientation >= 110 && orientation < 240) { //180度 倒竖屏
            return 2;
        } else if (orientation >= 240 && orientation < 300) { //270度 左横屏
            return 3;
        }
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mView = view;
        mCameraUI = new CameraUI(this);
        mIndex = mCameraUI.getmIndex();

        mTextureView = view.findViewById(R.id.texture);//获取mTextureView
        mCustomGridView = view.findViewById(R.id.customGridView);
        mIndicatorView = view.findViewById(R.id.indicatorView);
        mIndicatorView.init();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        releaseSoundPool();
        super.onPause();
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("showToast线程");
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("bruceCameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        faceHandleThread = new HandlerThread("faceHandle");
        faceHandleThread.start();
        faceHandle = new Handler(faceHandleThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.getLooper().quit();
            mBackgroundThread.interrupt();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;

            faceHandle.removeCallbacks(faceHandleRunable);
            faceHandle.removeCallbacksAndMessages(null);
            faceHandleThread.quitSafely();
            faceHandleThread.getLooper().quit();
            faceHandleThread.interrupt();
            faceHandleThread.join();
            faceHandleThread = null;
            faceHandle = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closeCaptureSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            stopBackgroundThread();
            mCameraOpenCloseLock.release();
        }
    }

    private void closeCaptureSession() {
        if (null != mCaptureSession) {
            try {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCaptureSession.close();
            mCaptureSession = null;
        }
        mPreviewRequestBuilder = null;
        mVideoPreviewBuilder = null;
        isPreview = false;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class 相机支持的尺寸list
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen 能够选择的最大宽度
     * @param maxHeight         The maximum height that can be chosen 能够选择的醉倒高度
     * @param aspectRatio       The aspect ratio 图像的比例(pictureSize, 只有当pictureSize和textureSize保持一致, 才不会失真)
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough 返回最合适的预览尺寸
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface// 存放小于等于限定尺寸, 大于等于texture控件尺寸的Size
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface// 存放小于限定尺寸, 小于texture控件尺寸的Size
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        // 1. 若存在bigEnough数据, 则返回最大里面最小的
        // 2. 若不存bigEnough数据, 但是存在notBigEnough数据, 则返回在最小里面最大的
        // 3. 上述两种数据都没有时, 返回空, 并在日志上显示错误信息
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return aspectRatio;
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices, String state) {
        boolean oSize = false;
        for (Size size : choices) {
            if (state.equals("1:1")) {
                oSize = size.getWidth() == size.getHeight();
                return new Size(1080, 1080);
            } else if (state.equals("4:3")) {
                oSize = size.getWidth() == size.getHeight() * 4 / 3;
                return new Size(1440, 1080);
            } else if (state.equals("16:9")) {
                oSize = size.getWidth() == size.getHeight() * 16 / 9;
                return new Size(1920, 1080);
            }
            if (oSize && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }


    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     * 屏幕方向发生改变时调用转换数据方法，
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     *                   TextureView通过此方法设置预览方向
     */
    private void configureTransform(int viewWidth, int viewHeight) {//配置transformation，主要是矩阵旋转相关
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);//设置mTextureView的transformation
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void setFlash(CaptureRequest.Builder requestBuilder) {  //设置闪光灯
        if (mFlashSupported) {
            switch (mFlashState) {
                case FLASH_STATE_FASLE:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                    break;

                case FLASH_STATE_TRUE:
                    Log.e(TAG, "case FLASH_STATE_TRUE:");
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                    break;

                case FLASH_STATE_AUTO:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);//标准自动曝光，闪光灯听从 HAL 指令开启，以进行预拍摄和静像拍摄。
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                    break;
            }
        }
    }

    /**
     * 获取对于尺寸分辨率大小
     *
     * @param size
     * @param state
     * @return
     */
    private boolean setJPEGSize(Size size, String state) {
        String countSize = countSize(size);
        if (state.equals(countSize)) {
            return true;
        }
        return false;
    }

    private String countSize(Size size) {
        int w = size.getWidth();
        int h = size.getHeight();
        int count = 0;
        while (true) {
            if (w == h) {
                w = 1;
                h = 1;
                break;
            }
            for (int i = 2; i < 10; i++) {
                if (w % i == 0 && h % i == 0) {
                    w = w / i;
                    h = h / i;
                    continue;
                }
                count++;
            }
            if (count == 8) {
                break;
            }
            count = 0;
        }
        return w + ":" + h;
    }

    private Size reSize() {
        if (openSize.equals("1:1")) {
            return new Size(1940, 1940);
        } else if (openSize.equals("4:3")) {
            return new Size(1440, 1080);
        } else if (openSize.equals("16:9")) {
            return new Size(1920, 1080);
        } else {
            return new Size(mTextureView.getHeight(), mTextureView.getWidth());
        }
    }

    private List<byte[]> imageQueue = new ArrayList<>();
    private class saveImage implements Runnable {

        private Image image;

        saveImage(Context context, Image image) {
            this.image = image;
        }

        @Override
        public void run() {
            Log.e(TAG, "保存图片的线程bitmap");
            Thread.currentThread().setName("保存图片的线程bitmap");
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            if (startContinuityPicture){
                imageQueue.add(bytes);
                image.close();
                return;
            }
            Bitmap bitmap = disposeByteToBitmap(bytes);
            saveImageToGallery(bytes, bitmap);
            image.close();
        }
    }

    private Bitmap disposeByteToBitmap(byte[] bytes){
        Log.e(TAG,"bitmap");
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Matrix m = new Matrix();
        int angle = 0;
        if (openCameraId.equals(Camera0)) {
            if (getOrientationSe(mSaveImageOrientation) == 0) {
                Log.e(TAG, "getOrientationSe(mSensorOrientation) == 0");
                angle = 90;
            } else if (getOrientationSe(mSaveImageOrientation) == 1) {
                Log.e(TAG, "getOrientationSe(mSensorOrientation) == 1----" + mSaveImageOrientation);
                angle = 180;
            } else if (getOrientationSe(mSaveImageOrientation) == 2) {
                Log.e(TAG, "getOrientationSe(mSensorOrientation) == 2");
                angle = -90;
            } else if (getOrientationSe(mSaveImageOrientation) == 3) {
                Log.e(TAG, "getOrientationSe(mSensorOrientation) == 3");
                angle = 0;
            } else {
                Log.e(TAG, "angle = 90;");
                angle = 90;
            }
        } else if (openCameraId.equals(Camera1)) {
            if (getOrientationSe(mSaveImageOrientation) == 0) {
                angle = -90;
            } else if (getOrientationSe(mSaveImageOrientation) == 1) {
                angle = -180;
            } else if (getOrientationSe(mSaveImageOrientation) == 2) {
                angle = -270;
            } else if (getOrientationSe(mSaveImageOrientation) == 3) {
                angle = 0;
            } else {
                angle = -90;
            }
        }

        Log.e(TAG, "" + angle);

        m.setRotate(angle, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

        Matrix m2 = new Matrix();
        if (openCameraId.equals("1")) {
            m2.postScale(-1, 1);        //镜像水平翻转
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m2, true);

        if (openLOOK == OPENLOOK_TRUE) {
            bitmap = createWatermark(bitmap, format);
        }
        return bitmap;
    }

    public void saveImageToGallery(byte[] bytes, Bitmap bitmap) {
        Log.e(TAG, "saveImageToGallery");
        // 首先保存图片
        String fileName = "sign_" + System.currentTimeMillis() + ".jpg";
        File file = new File(headPath, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream outputStream = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.e(TAG, "保存图片到文件");
            Log.e(TAG, headPath);

            if (isUpdateThumbnail){
                ImageView imageView = mCameraUI.getmImageView();
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 500, 500);
                imagePath = file.getPath();
                Log.e(TAG, "imageView.getWidth() = " + imageView.getWidth() + "     imageView.getHeight() = " + imageView.getHeight());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(thumbnail);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap createWatermark(Bitmap bitmap, String mark) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        //绘制图像
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.save();

        Paint p = new Paint();
        // 水印颜色
        p.setColor(Color.WHITE);
        // 水印字体大小
        p.setTextSize(50);
        //抗锯齿
        p.setAntiAlias(true);
        p.setDither(true);
        p.setFilterBitmap(true);

        Rect rectText = new Rect();
        p.getTextBounds(mark, 0, mark.length(), rectText);
        int beginX = bitmap.getWidth() - rectText.width() - 50;
        int beginY = bitmap.getHeight() - rectText.height() - 20;

        //绘制文字
        canvas.drawText(mark, beginX, beginY, p);
        canvas.restore();
        return bmp;
    }

    private void releaseSoundPool(){
        if (mSoundPool != null) {
            mSoundPool.unload(R.raw.picture);
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.e(TAG, "onImageAvailable");
            mBackgroundHandler.post(new saveImage(getActivity(), reader.acquireLatestImage()));   //通知线程保存图片
        }
    };

    private void requestCameraPermission() {
        Log.e(TAG, "requestCameraPermission");
        while (true){
            int count = 0;
            for (String permission : permissionsREAD) {
                boolean b = ContextCompat.checkSelfPermission(getContext(), permission) ==
                        PackageManager.PERMISSION_DENIED;
                if (b){
                    requestPermissions(permissionsREAD, REQUEST_CAMERA_PERMISSION);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                count ++;
                Log.e(TAG, "count = " + count);
            }
            if (count == 4) {
                return;
            }
        }
    }


    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {//TextureView回调
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable");
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureSizeChanged");
            configureTransform(width, height);// 当屏幕旋转时，预览方向改变时, 执行转换操作. 默认情况下TextureView通过此方法设置预览方向
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }//可获取bitmap
    };

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.相机状态改变的回调函数
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {//打开相机设备状态回调

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {//打开成功
            Log.e(TAG, "onOpened");
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();//释放访问许可
            mCameraDevice = cameraDevice;//从onOpened参数获取mCameraDevice
            startPreview();//创建会话
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {//断开相机
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {//打开错误
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }

        try {
            closeCaptureSession();
            List<Surface> surfaces = null;
            SurfaceTexture texture = mTextureView.getSurfaceTexture();//通过mTextureView获取SurfaceTexture。
            assert texture != null;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置SurfaceTexture大小
            Surface surface = new Surface(texture);//通过SurfaceTexture创建Surface来预览。
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//创建TEMPLATE_PREVIEW预览模板CaptureRequest.Builder

            if (mIndex == VIDEO_MODULE_INDEX) {
                if (openVideoSize == VIDEO_SIZE_HIGHFPS){
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(60, 60));
                }else{
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(30, 30));
                }
                surfaces = Collections.singletonList(surface);
            } else if (mIndex == PHOTO_MODULE_INDEX) {
                surfaces = Arrays.asList(surface, mImageReader.getSurface());
            }
            mPreviewRequestBuilder.addTarget(surface);//CaptureRequest.Builder中添加Surface，即mTextureView获取创建的Surface

            mCameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (null == mCameraDevice) {
                                return;
                            }
                            mCaptureSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {//创建会话失败
                            showToast("创建会话失败");
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            isPreview = true;
            if (mIndex == VIDEO_MODULE_INDEX){
                mCameraUI.getmFaceView().setFaces(null);
            }else if (mIndex == PHOTO_MODULE_INDEX){
                startFaceDetec();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVideoPreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            mVideoPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            if (openVideoSize == VIDEO_SIZE_HIGHFPS){
                mVideoPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(60, 60));
            }else {
                mVideoPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(30, 30));
            }

            mCaptureSession.setRepeatingRequest(mVideoPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openCamera(int width, int height) {
        startBackgroundThread();//为相机开启了一个后台线程，这个进程用于后台执行相关的工作
        Log.e(TAG, "openCamera");
        mMediaRecorder = new MediaRecorder();
        setUpCameraOutputs(width, height);//设置最佳预览分辨率
        configureTransform(width, height);//TextureView通过此方法设置预览方向
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume()");
        super.onResume();
        mContext = getContext();
        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        mBeepPicture = mSoundPool.load(mContext, R.raw.picture, 1);
        mBeepVideo = mSoundPool.load(mContext, R.raw.video_record, 1);

        if (mOrientationListener.canDetectOrientation()) {//判断设备是否支持
            mOrientationListener.enable();
            Log.e(TAG, "mOrientationListener.enable();！");
        } else {
            mOrientationListener.disable();//注销
            Log.e(TAG, "当前设备不支持手机旋转！");
        }
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    private void setUpCameraOutputs(int width, int height) {
        Log.e(TAG, "setUpCameraOutputs");
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraIdList = manager.getCameraIdList();
            for (String cameraId : cameraIdList) {

                if (!cameraId.equals(openCameraId)) {
//                    Log.e(TAG,cameraId);
                    continue;
                }
                Log.e(TAG, cameraId);

                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

//                Size[] sizes = map.getOutputSizes(MediaRecorder.class);
//                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class), openSize);

                mVideoSize = new Size(1920, 1080);
                if (mIndex == VIDEO_MODULE_INDEX) {
                    mPreviewSize = mVideoSize;
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mCustomGridView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mCameraUI.getmFaceView().setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mCameraId = cameraId;
                    break;
                }

                //获取需要的分辨率尺寸
                Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
                List<Size> list2 = new ArrayList<>();
                List<Size> list = new ArrayList<>(Arrays.asList(outputSizes));
                Log.e(TAG, "获取需要的分辨率尺寸");
                if (openSize.equals("全屏")) {
                    list.clear();
                } else {
                    for (Size size : outputSizes) {
                        if (!setJPEGSize(size, openSize)) {
                            list2.add(size);
                        }
                    }
                    list.removeAll(list2);
                }
                Size size = reSize();
                if (list.size() == 0) {
                    list.add(size);
                }


                // For still image captures, we use the largest available size.对于静态图像捕捉，我们使用最大的可用尺寸
                Size largest = Collections.max(list, new CompareSizesByArea());


                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);//设置ImageReader接收的图片格式，以及允许接收的最大图片数目
                mImageReader.setOnImageAvailableListener(
                        onImageAvailableListener, mBackgroundHandler);//设置图片存储的监听，但在创建会话，调用capture后才能有数据

                // coordinate.看看我们是否需要交换尺寸，以获得相对于传感器坐标的预览大小。
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();//获取屏幕显示方向
                mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);//获取sensor方向
                boolean swappedDimensions = false;//交换尺寸
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);//Android获取屏幕分辨率displaySize
                int maxPreviewWidth = displaySize.x;    //获取屏幕分辨率的宽
                int maxPreviewHeight = displaySize.y;    //获取屏幕分辨率的高

                if (swappedDimensions) {// 如果需要进行画面旋转, 将宽度和高度对调
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                mPreviewSize = chooseOptimalSize(outputSizes, width, height, maxPreviewWidth, maxPreviewHeight, largest);
                if (mPreviewSize.getWidth() > size.getWidth() && mPreviewSize.getHeight() > size.getHeight()) {
                    mPreviewSize = size;
                }
//                mPreviewSize = new Size(largest.getWidth(), largest.getHeight());

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {// 如果方向是横向(landscape)
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置TextureView预览分辨率。
                    mCustomGridView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mCameraUI.getmFaceView().setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mCameraUI.getmTimerView().setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {// 方向不是横向(即竖向)
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mCustomGridView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mCameraUI.getmFaceView().setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mCameraUI.getmTimerView().setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }



                // Check if the flash is supported.检查是否支持flash。
                Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;//获取当前ID
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiate a still image capture.
     */
    public void takePicture() {//拍照
        Log.e(TAG,"takePicture() {//拍照 --> mIndex = " + mIndex);
        if (mIndex == VIDEO_MODULE_INDEX) {
            if (isRunVideo) {
                stopRecordingVideo();
                isRunVideo = false;
            } else {
                startRecordingVideo();
                isRunVideo = true;
            }
        } else if (mIndex == PHOTO_MODULE_INDEX) {
            lockFocus();
        } else if (mIndex == PORTRAIT_MODULE_INDEX) {

        }

    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {//拍照过程中锁住焦点
        try {
//            showToast("拍照中--");
            // Tell #mCaptureCallback to wait for the lock.通知mCaptureCallback等待锁定

            if (mCameraId.equals("1")) {
                mCaptureSession.stopRepeating();//停止预览,停止任何一个正常进行的重复请求。
                mCaptureSession.abortCaptures();//中断Capture,尽可能快的取消当前队列中或正在处理中的所有捕捉请求。
            }
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,//触发 AF 扫描
                    CameraMetadata.CONTROL_AF_TRIGGER_START);//触发 AF 扫描的启动操作。扫描效果取决于模式和状态。

            mState = STATE_WAITING_LOCK;//设置等待自动对焦的焦点被锁状态,通知mCaptureCallback等待锁定
            

            if (mFlashSupported ) {
                Log.e(TAG, "mFlashState = " + mFlashState);
                switch (mFlashState) {
                    case FLASH_STATE_FASLE:
                        break;

                    case FLASH_STATE_TRUE:
                    case FLASH_STATE_AUTO:
                        Log.e(TAG, "FLASH_STATE_TRUE | FLASH_STATE_AUTO:");
                        setFlash(mPreviewRequestBuilder);
                        break;
                }
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
                return;
            }
            Log.e(TAG,"lockFocus  return");
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);//通知camera锁住对焦和状态通过mPreviewRequestBuilder.build()只发送一次请求，
            // 而不是mPreviewRequest。是同一个回调mCaptureCallback，发送一次请求只是等待自动对焦的焦点被锁，切换为STATE_WAITING_LOCK再真正进行拍照
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {//拍照回调

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {//预览状态，则什么都不做
                    break;
                }
                case STATE_WAITING_LOCK: {//等待自动对焦的焦点被锁时，由设置拍照流时设置的STATE_WAITING_LOCK
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);//获取当前 AF 算法状态
                    Log.e(TAG, "afState = " + afState);
                    if (afState == null) {//某些设备完成锁定后CONTROL_AF_STATE可能为null
                        mState = STATE_PICTURE_TAKEN;//设置拍照状态，APP开始获取图片数据流进行保存
                        captureStillPicture();//进行拍照
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||    /*AF 算法认为已对焦。镜头未移动。*/
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {    /*AF 算法认为无法对焦。镜头未移动。*/
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);//获取当前 AF 算法状态
                        Log.e(TAG, "aeState = " + aeState);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {//AE 已经为当前场景找到了理想曝光值，且曝光参数不会变化。
                            mState = STATE_PICTURE_TAKEN;//设置拍照状态，APP开始获取图片数据流进行保存
                            captureStillPicture();
                        } else {
                            //如果没有找到理想曝光值，则运行捕获静止图像的预捕获序列操作。
                            try {
                                Log.e(TAG, "没有找到理想曝光值");
                                // This is how to tell the camera to trigger.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,//用于在拍摄高品质图像之前启动测光序列的控件。
                                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);//启动预拍序列。HAL 应使用后续请求进行衡量并达到理想的曝光/白平衡，以便接下来拍摄高分辨率的照片。
                                mState = STATE_WAITING_PRECAPTURE;//设置等待曝光为预捕获状态
                                mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                                        mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {//等待曝光为预捕获状态
                    // CONTROL_AE_STATE can be null on some devices某些设备CONTROL_AE_STATE可能为null
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);//获取当前 AE 算法状态
                    Log.e(TAG, "aeState = " + aeState);
                    if (aeState == null ||  //某些设备CONTROL_AE_STATE可能为null
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||    /*HAL 正在处理预拍序列。*/
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {/*HAL 已聚焦曝光，但认为需要启动闪光灯才能保证照片亮度充足。*/
                        mState = STATE_WAITING_NON_PRECAPTURE;//设置等待曝光不是预捕获状态
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // 某些设备CONTROL_AE_STATE可能为null
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);//获取当前 AE 算法状态
                    Log.e(TAG, "aeState = " + aeState);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {/*HAL 正在处理预拍序列。*/
                        mState = STATE_PICTURE_TAKEN;//设置拍照状态，APP开始获取图片数据流进行保存
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    private void captureStillPicture() {//进行拍照
        final Activity activity = getActivity();
        if (null == activity || null == mCameraDevice) {
            return;
        }

        try {
            final CaptureRequest.Builder captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequest.addTarget(mImageReader.getSurface());

            captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            if (mFlashState == FLASH_STATE_TRUE || mFlashState == FLASH_STATE_AUTO){
                Log.e(TAG, "captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);");
                setFlash(captureRequest);
            }

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureRequest.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));


            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.e(TAG, "拍照回调成功");
                    unlockFocus();
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.e(TAG, "拍照回调失败");
                }
            };
            Log.e(TAG, "isContinuityPicture = " + isContinuityPicture);
            if (isContinuityPicture){
                Handler handler = new ContinuityPictureHandle();
                mPictureRequest = captureRequest;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean is = true;
                        int i = 0;
                        while (is){
                            if (startContinuityPicture){
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                                i ++;
                            }
                            if (!isContinuityPicture){
                                is = false;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e(TAG, "连续拍照完成，拍摄照片数量为： " + i);
                        showToast("连拍照片保存中");
                        unlockFocus();
                        isUpdateThumbnail = false;
                        for (int i1 = 0; i1 < imageQueue.size(); i1++) {
                            if (i1 == imageQueue.size() - 1){
                                isUpdateThumbnail = true;
                            }
                            byte[] bytes = imageQueue.get(i1);
                            saveImageToGallery(bytes, disposeByteToBitmap(bytes));
                        }
                    }
                }).start();

            }else {
                mSoundPool.play(mBeepPicture, 1.0f, 1.0f, 0, 0, 1.0f);
                mCaptureSession.capture(captureRequest.build(), captureCallback, mBackgroundHandler);//进行拍照
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);//取消当前 AF 扫描（如有），并将算法重置为默认值。
            mState = STATE_PREVIEW;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), null, mBackgroundHandler);//通过mPreviewRequestBuilder.build()只发送一次请求，而不是mPreviewRequest。
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存录像视频的参数
     */
    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mNextVideoAbsolutePath = headPath + "VID_" + System.currentTimeMillis() + ".mp4";
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        if (openVideoSize == VIDEO_SIZE_HIGHFPS){
            mMediaRecorder.setVideoFrameRate(60);
        }else {
            mMediaRecorder.setVideoFrameRate(30);
        }
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    /**
     * 开始录像
     */
    private void startRecordingVideo() {
        Log.e(TAG, "startRecordingVideo");
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closeCaptureSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
//            texture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
            texture.setDefaultBufferSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mVideoPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            Surface videoPreviewSurface = new Surface(texture);
            surfaces.add(videoPreviewSurface);
            mVideoPreviewBuilder.addTarget(videoPreviewSurface);

            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mVideoPreviewBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    updateVideoPreview();
                    mSoundPool.play(mBeepVideo, 1.0f, 1.0f, 0, 0, 1.0f);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    showToast("录像会话创建失败！！！");
                }
            }, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录像
     */
    private void stopRecordingVideo() {
        try {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        } catch (RuntimeException e) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        } catch (Exception e) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        }

        mMediaRecorder.reset();
        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }

        mSoundPool.play(mBeepVideo, 1.0f, 1.0f, 0, 0, 1.0f);
        Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(mNextVideoAbsolutePath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imagePath = mNextVideoAbsolutePath;
                mCameraUI.getmImageView().setImageBitmap(videoThumbnail);
            }
        });
        mNextVideoAbsolutePath = null;
        startPreview();
    }


    private AlertDialog mGridAlertDialog; //单选框
    private int mDialogType = 0;

    public void showSingleAlertDialog(int clickId) {
        onClickId = clickId;
        String title = null;
        String[] items = null;

        switch (onClickId) {
            case PHOTOSIZE_ONCLICK:
                title = "照片分辨率";
                items = new String[]{"1:1(4512 * 4512)", "3:4(3024 * 4032)", "9:16(3240 * 5760)"};
                if (openSize.equals(PHOTO_SIZE_ONE)) {
                    mDialogType = 0;
                } else if (openSize.equals(PHOTO_SIZE_TWO)) {
                    mDialogType = 1;
                } else if (openSize.equals(PHOTO_SIZE_THREE)) {
                    mDialogType = 2;
                }
                break;
            case FLASHLIGHT_ONCLICK:
                title = "闪光灯";
                items = new String[]{"关闭", "开启", "自动"};
                mDialogType = mFlashState;
                break;
            case GRID_ONCLICK:
                title = "网格";
                items = new String[]{"关", "九宫格", "黄金比例"};
                if (gridType == GRID_TYPE1) {
                    mDialogType = 1;
                } else if (gridType == GRID_TYPE2) {
                    mDialogType = 2;
                } else {
                    mDialogType = 0;
                }
                break;

            case LOOK_ONCLICK:
                title = "水印";
                items = new String[]{"关闭", "开启"};
                mDialogType = openLOOK;
                break;

            case VIDEOSIZE_ONCLICK:
                title = "录像分辨率";
                items = new String[]{"1080P 60FPS", "1080P 30FPS"};
                mDialogType = openVideoSize;
                break;

            case TIMECLOCK_ONCLICK:
                title = "定时器";
                items = new String[]{"关闭", "5秒", "10秒"};
                mDialogType = mTimerState;
                break;
        }
        int lastT = mDialogType;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mView.getContext());
        alertBuilder.setTitle(title);
        alertBuilder.setSingleChoiceItems(items, mDialogType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDialogType = i;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mDialogType != lastT) {
                    switch (onClickId) {
                        case PHOTOSIZE_ONCLICK:
                            if (mDialogType == 0) {
                                openSize = PHOTO_SIZE_ONE;
                            } else if (mDialogType == 1) {
                                openSize = PHOTO_SIZE_TWO;
                            } else if (mDialogType == 2) {
                                openSize = PHOTO_SIZE_THREE;
                            }
                            mCameraUI.updateUI(PHOTOSIZE_ONCLICK);
                            resetCamera();
                            break;

                        case FLASHLIGHT_ONCLICK:
                            mFlashState = mDialogType;
                            mCameraUI.updateUI(FLASHLIGHT_ONCLICK);
                            break;

                        case GRID_ONCLICK:
                            gridType = mDialogType;
                            if (gridType != 0) {
                                mCustomGridView.setState(true);
                                mCustomGridView.setStyle(gridType);
                            } else {
                                mCustomGridView.setState(false);
                            }
                            mCameraUI.updateUI(GRID_ONCLICK);
                            break;

                        case LOOK_ONCLICK:
                            openLOOK = mDialogType;
                            mCameraUI.updateUI(LOOK_ONCLICK);
                            break;

                        case VIDEOSIZE_ONCLICK:
                            openVideoSize = mDialogType;
                            mCameraUI.updateUI(VIDEOSIZE_ONCLICK);
                            resetCamera();
                            break;

                        case TIMECLOCK_ONCLICK:
                            mTimerState = mDialogType;
                            mCameraUI.updateUI(TIMECLOCK_ONCLICK);
                            break;
                    }
                }
                mGridAlertDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mGridAlertDialog.dismiss();
            }
        });

        mGridAlertDialog = alertBuilder.create();
        mGridAlertDialog.show();
    }

    public void setModule() {
        switch (mIndex) {
            case VIDEO_MODULE_INDEX:
                resetCamera();
                break;
            case PHOTO_MODULE_INDEX:
                resetCamera();
                break;
            case PORTRAIT_MODULE_INDEX:
                break;
        }
    }

    private void resetCamera() {
        closeCamera();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        openCamera(mTextureView.getWidth(), mTextureView.getHeight());
    }

    private Handler faceHandle;
    private HandlerThread faceHandleThread;
    private final FaceHandle faceHandleRunable = new FaceHandle();
    private final int MAX_FACE_COUNT = 50;
    private void startFaceDetec(){
        Log.e(TAG, "startFaceDetec" + isPreview);
        this.faceHandle.post(faceHandleRunable);
    }

    private class FaceHandle implements Runnable{
        @Override
        public void run() {
            FaceDetector faceDetector = new FaceDetector(mTextureView.getWidth(), mTextureView.getHeight(), MAX_FACE_COUNT);
            Bitmap bitmap = mTextureView.getBitmap();
            Bitmap cBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            PointF midpoint = new PointF();

            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE_COUNT];

            int count = faceDetector.findFaces(cBitmap, faces);
//            Log.e(TAG, "人脸为：" + count);
            ArrayList<RectF> faceList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                faces[i].getMidPoint(midpoint);
                float x = midpoint.x;
                float y = midpoint.y;
                float v = faces[i].eyesDistance();
                y = y + (v / 2);
//                    Log.e(TAG, "x = " + x + "    y = "+ y + "    v = "+ v);

                float left = x - v;
                float top = y - v;
                float right = x + v;
                float bottom = y + v;

                RectF rectF = new RectF(left, top, right, bottom);
                faceList.add(rectF);
            }
            mCameraUI.getmFaceView().setFaces(faceList);

            faceHandle.postDelayed(this, 10);
        }
    }

    private boolean isContinuityPicture = false;        //是否连拍
    public void setContinuityPicture(boolean continuityPicture) {
        isContinuityPicture = continuityPicture;
    }
    private boolean startContinuityPicture = false;     //是否开始连拍
    public void setStartContinuityPicture(boolean b){
        startContinuityPicture = b;
    }
    private CaptureRequest.Builder mPictureRequest;     //拍照的请求构造器
    private class ContinuityPictureHandle extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    Log.e(TAG, "连拍中——");
                    try {
                        mSoundPool.play(mBeepPicture, 1.0f, 1.0f, 0, 0, 2.0f);
                        mCaptureSession.capture(mPictureRequest.build(), null, this);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}


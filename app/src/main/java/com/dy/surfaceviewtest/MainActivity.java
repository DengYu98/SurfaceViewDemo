package com.dy.surfaceviewtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    //远端的视图
    private SurfaceView surfaceviewRemote;

    //本地的视图
    private SurfaceView surfaceviewLocal;

    //本地视图大小
    private RelativeLayout rlRemote;
    private RelativeLayout rlLocal;

    //拨打电话状态栏
    private LinearLayout llCallContainer;

    //默认本地视图的状态
    private boolean mSate = true;

    //默认本地视频宽度  90dp
    private int defaultLocalwidth = 90;
    private int defaultLocalHeight;

    //默认本地视频边距  20dp
    private int defaultLocalMargin = 20;

    private Handler uiHandler;

    //视频源1 2
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //如果判断有刘海屏不让填充到状态栏
        if (DisplayUtil.hasNotchScreen(this)) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_main);

        llCallContainer = (LinearLayout) findViewById(R.id.ll_call_container);
        rlRemote = (RelativeLayout) findViewById(R.id.rl_remote);
        surfaceviewRemote = (SurfaceView) findViewById(R.id.surfaceview_remote);
        rlLocal = (RelativeLayout) findViewById(R.id.rl_local);
        surfaceviewLocal = (SurfaceView) findViewById(R.id.surfaceview_local);

        surfaceviewRemote.setOnClickListener(this);
        surfaceviewLocal.setOnClickListener(this);

        //初始化视频源
        mediaPlayer = new MediaPlayer();
        mediaPlayer2 = new MediaPlayer();

        SurfaceHolder remoteHolder = surfaceviewRemote.getHolder();
        remoteHolder.setFormat(PixelFormat.TRANSPARENT);
        remoteHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
//                int width = holder.getSurfaceFrame().width();
//                int height = holder.getSurfaceFrame().height();
//                int textSize;
//                if (width <= defaultLocalwidth || height <= defaultLocalHeight) {
//                    textSize = 50;
//                } else {
//                    textSize = 80;
//                }
//
//                //画矩形
//                Canvas c = holder.lockCanvas();
//                Paint paint = new Paint();
//                paint.setColor(Color.RED);
//                paint.setStyle(Paint.Style.FILL);
//                Rect rect = new Rect(0, 0, width, height);
//                c.drawRect(rect, paint);
//
//                //书写文字
//                paint.setColor(Color.WHITE);
//                paint.setTextSize(textSize);
//                //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
//                paint.setTextAlign(Paint.Align.CENTER);
//
//                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
//                float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
//                float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,
//
//                int baseLineY = (int) (rect.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公
//                c.drawText("远程视图", rect.centerX(), baseLineY, paint);
//                //解锁画布 更新提交屏幕显示内容
//                holder.unlockCanvasAndPost(c);
                play2();
                Log.e("surfaceviewRemote", "surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                Log.e("surfaceviewRemote", "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                pause2();
                Log.e("surfaceviewRemote", "surfaceDestroyed");
            }
        });

        surfaceviewLocal.setZOrderOnTop(true);
        surfaceviewLocal.setZOrderMediaOverlay(true);
        SurfaceHolder localHolder = surfaceviewLocal.getHolder();
        localHolder.setFormat(PixelFormat.TRANSPARENT);
        initCamera();
        localHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceHolder = holder;

//                int width = holder.getSurfaceFrame().width();
//                int height = holder.getSurfaceFrame().height();
//                int textSize;
//                if (width <= defaultLocalwidth || height <= defaultLocalHeight) {
//                    textSize = 50;
//                } else {
//                    textSize = 80;
//                }
//
//                Canvas c = holder.lockCanvas();
//                Paint paint = new Paint();
//                paint.setColor(Color.YELLOW);
//                Rect rect = new Rect(0, 0, width, height);
//                c.drawRect(rect, paint);
//
//                paint.setColor(Color.GRAY);
//                paint.setTextSize(textSize);
//                paint.setTextAlign(Paint.Align.CENTER);
//
//                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
//                float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
//                float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,
//
//                int baseLineY = (int) (rect.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公
//                c.drawText("本地视图", rect.centerX(), baseLineY, paint);
//
//                // 解锁画布 更新提交屏幕显示内容
//                holder.unlockCanvasAndPost(c);

                play();
                Log.e("surfaceviewLocal", "surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                Log.e("surfaceviewLocal", "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                pause();
                Log.e("surfaceviewLocal", "surfaceDestroyed");
            }
        });


        defaultLocalMargin = DisplayUtil.dip2px(this, defaultLocalMargin);
        defaultLocalwidth = DisplayUtil.dip2px(this, defaultLocalwidth);
        defaultLocalHeight = (int) (DisplayUtil.getScreenHeight(this) / (float) DisplayUtil.getScreenWidth(this) * defaultLocalwidth);
        zoomOpera(rlRemote, surfaceviewRemote, rlLocal, surfaceviewLocal);

        uiHandler = new Handler();
        postDelayedCloseBtn();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (mediaPlayer2 != null) {
            mediaPlayer2.stop();
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }

        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }

        super.onDestroy();
    }

    /**
     * 播放源 /sdcard/DCIM/Camera/VID_20221119_204927.mp4
     */
    private void play() {
        startPreview();
//        File file = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera/VID_20221119_204927.mp4");
//        Uri uri = Uri.fromFile(file);
//        try {
//            mediaPlayer.reset();
////            Uri mVideoUri = Uri.parse("rtmp://livetv.dhtv.cn/live/peoples");
//            mediaPlayer.setDataSource(this, uri);
//            //设置视频输出到SurfaceView
//            mediaPlayer.setDisplay(surfaceviewLocal.getHolder());
//            mediaPlayer.prepare();
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mediaPlayer.start();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 播放源2
     */
    private void play2() {
        File file = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera/VID_20221103_223922.mp4");
        Uri uri = Uri.fromFile(file);
        try {
            mediaPlayer2.reset();
            // /sdcard/DCIM/Camera/VID_20221103_223922.mp4
//            Uri mVideoUri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
            mediaPlayer2.setDataSource(this, uri);
            //设置视频输出到SurfaceView
            mediaPlayer2.setDisplay(surfaceviewRemote.getHolder());
            mediaPlayer2.prepare();
            mediaPlayer2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer2.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 暂停1
     */
    private void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    /**
     * 暂停1
     */
    private void pause2() {
        if (mediaPlayer2.isPlaying()) {
            mediaPlayer2.pause();
        } else {
            mediaPlayer2.start();
        }
    }

    /**
     * 停止
     */
    private void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    /**
     * 停止
     */
    private void stop2() {
        if (mediaPlayer2 != null && mediaPlayer.isPlaying()) {
            mediaPlayer2.stop();
            mediaPlayer2.release();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.surfaceview_local:
                Log.d(TAG, " onClick surfaceview_local" + mSate);
                if (mSate) {
                    zoomOpera(rlLocal, surfaceviewLocal, rlRemote, surfaceviewRemote);
                    mSate = false;
                } else {
                    zoomOpera(rlRemote, surfaceviewRemote, rlLocal, surfaceviewLocal);
                    mSate = true;
                }
                break;
            case R.id.surfaceview_remote:
                Log.d(TAG, " onClick surfaceview_remote" + mSate);
                if (!mSate) {
                    zoomOpera(rlRemote, surfaceviewRemote, rlLocal, surfaceviewLocal);
                    mSate = true;
                } else {
                    changeStatus();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 改变转态烂的显示隐藏
     */
    protected void changeStatus() {
        if (llCallContainer.getVisibility() == View.GONE) {
            llCallContainer.setVisibility(View.VISIBLE);
            showStatusBar();
            postDelayedCloseBtn();
        } else {
            llCallContainer.setVisibility(View.GONE);
            hideStatusBar();
        }
    }

    /**
     * 大小视图切换 （小视图在前面、大视图在后面）
     *
     * @param sourcView  之前相对布局大小
     * @param beforeview 之前surfaceview
     * @param detView    之后相对布局大小
     * @param afterview  之后的surfaceview
     */
    private void zoomOpera(View sourcView, SurfaceView beforeview,
                           View detView, SurfaceView afterview) {
        RelativeLayout paretview = (RelativeLayout) sourcView.getParent();
//        paretview.removeView(detView);
//        paretview.removeView(sourcView);

        //设置远程大视图RelativeLayout 的属性
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        beforeview.setZOrderMediaOverlay(false);
        beforeview.getHolder().setFormat(PixelFormat.TRANSPARENT);
        sourcView.setLayoutParams(params1);

        //设置本地小视图RelativeLayout 的属性
        params1 = new RelativeLayout.LayoutParams(defaultLocalwidth, defaultLocalHeight);
        params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params1.setMargins(0, defaultLocalMargin, defaultLocalMargin, 0);
        //在调用setZOrderOnTop(true)之后调用了setZOrderMediaOverlay(true)  遮挡问题
        afterview.setZOrderOnTop(true);
        afterview.setZOrderMediaOverlay(true);
        afterview.getHolder().setFormat(PixelFormat.TRANSPARENT);
        detView.setLayoutParams(params1);

//        paretview.addView(sourcView);
//        paretview.addView(detView);
    }

    /**
     * 开启取消延时动画
     */
    private void postDelayedCloseBtn() {
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                llCallContainer.setVisibility(View.GONE);
                hideStatusBar();
            }
        }, 5000);
    }

    /**
     * 隐藏标题栏
     */
    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    /**
     * 显示标题栏
     */
    private void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    private CameraDevice mCameraDevice;
    private SurfaceHolder surfaceHolder = null;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler childHandler;
    private void initCamera() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
//        mainHandler = new Handler(getMainLooper());
        CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                            11);
                }
                //return;
            } else {
                //打开摄像头
                mCameraManager.openCamera("0", new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        mCameraDevice = camera;
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        if(mCameraDevice != null) {
                            mCameraDevice.close();
                            mCameraDevice = null;
                        }
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {

                    }
                }, childHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surfaceHolder.getSurface());

            ArrayList<Surface> surfaces = new ArrayList<>();
            surfaces.add(surfaceHolder.getSurface());
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);


                        ImageReader mImageReader = ImageReader.newInstance(1920, 1080,
                                ImageFormat.YUV_420_888, 2);
                        mImageReader.setOnImageAvailableListener(
                                new OnImageAvailableListenerImpl(), childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class OnImageAvailableListenerImpl implements ImageReader.OnImageAvailableListener {
        private byte[] y;
        private byte[] u;
        private byte[] v;
        private ReentrantLock lock = new ReentrantLock();

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            // Y:U:V == 4:2:2
            if (image.getFormat() == ImageFormat.YUV_420_888) {
                Image.Plane[] planes = image.getPlanes();
                // 加锁确保y、u、v来源于同一个Image
                lock.lock();
                // 重复使用同一批byte数组，减少gc频率
                if (y == null) {
                    y = new byte[planes[0].getBuffer().limit() - planes[0].getBuffer().position()];
                    u = new byte[planes[1].getBuffer().limit() - planes[1].getBuffer().position()];
                    v = new byte[planes[2].getBuffer().limit() - planes[2].getBuffer().position()];
                }
                if (image.getPlanes()[0].getBuffer().remaining() == y.length) {
                    planes[0].getBuffer().get(y);
                    planes[1].getBuffer().get(u);
                    planes[2].getBuffer().get(v);
//                    camera2Listener.onPreview(y, u, v, mPreviewSize, planes[0].getRowStride());
                    Log.i("dengyu", "onImageAvailable: ");
                }
                lock.unlock();
            }
            image.close();
        }
    }
}
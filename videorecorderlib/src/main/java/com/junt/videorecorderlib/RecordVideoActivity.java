package com.junt.videorecorderlib;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

public class RecordVideoActivity extends AppCompatActivity implements
        View.OnTouchListener, View.OnClickListener,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {
    private final String TAG = RecordVideoActivity.class.getSimpleName();
    public static final String VIDEO_PATH = "video_path";
    public static final int REQUEST_CODE_TO_PLAY = 999;

    private int MAX_DURATION = 6 * 1000;
    private long startTime, endTime;
    private ImageView ivBack;
    private Button btnRecord;
    private CustomProgressBar progressBar;
    private SurfaceView surfaceView;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private int maxZoom;
    private int minZoom;
    private File mOutputFile;
    private ProgressHandler progressHandler;
    private boolean isRecording = false;
    private SharedPreferences sp;
    private long clickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record_video);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaRecorder == null) {
            new MediaPrepareTask().execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
    }

    /**
     * 初始化任务（Camera,MediaRecorder）
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean initResult = prepareVideoRecorder();
            Log.d(TAG, "doInBackground，init:" + initResult);
            if (initResult) {
                Log.d(TAG, "开始预览");
                return true;
            } else {
                Log.d(TAG, "初始化错误");
                releaseMediaRecorder();
                return false;
            }
        }
    }

    /**
     * 初始化MediaRecorder
     */
    private boolean prepareVideoRecorder() {
        Log.d(TAG, "prepareVideoRecorder");
        mCamera = Camera.open();
        Log.d(TAG, "Camera.open");
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, surfaceView.getWidth(), surfaceView.getHeight());
        mCamera.setDisplayOrientation(90);
        CamcorderProfile profile = CamcorderProfile.get(sp.getInt(RecordConfig.CONFIG_QUALITY, CamcorderProfile.QUALITY_480P));
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;
        profile.videoBitRate = sp.getInt(RecordConfig.CONFIG_ENCODING_BIT_RATE, 5 * 1280 * 720);
        profile.videoFrameRate = sp.getInt(RecordConfig.CONFIG_FRAME_RATE, 30);
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        parameters.setFocusMode(sp.getString(RecordConfig.CONFIG_FOCUS_MODE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO));
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            mCamera.startPreview();
            Log.d(TAG, "Camera初始化结束");
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
        }

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(sp.getInt(RecordConfig.CONFIG_QUALITY, CamcorderProfile.QUALITY_480P)));

//        mMediaRecorder.setVideoFrameRate(sp.getInt(RecordConfig.CONFIG_FRAME_RATE, 30));
//        mMediaRecorder.setVideoEncodingBitRate(sp.getInt(RecordConfig.CONFIG_ENCODING_BIT_RATE, 5 * 1280 * 720));

        String defaultPath = Environment.getExternalStorageDirectory() + "/junt/video/";
        String outputPath = sp.getString(RecordConfig.CONFIG_OUTPUT_PATH, defaultPath) + "VID_" + System.currentTimeMillis() + ".mp4";
        mOutputFile = new File(outputPath);
        if (!mOutputFile.getParentFile().exists()) {
            mOutputFile.getParentFile().mkdirs();
        }
        mMediaRecorder.setOutputFile(mOutputFile.getAbsolutePath());
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        MAX_DURATION = sp.getInt(RecordConfig.CONFIG_MAX_DURATION, 6 * 1000);
        mMediaRecorder.setMaxDuration(MAX_DURATION);
        try {
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.prepare();
        } catch (Exception e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.d(TAG, "onInfo what=" + what + " extra=" + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED && isRecording) {
            recordComplete();
        }
    }

    /**
     * 录制结束
     */
    private void recordComplete() {
        isRecording = false;
        btnRecord.setPressed(false);
        endTime = System.currentTimeMillis();
        Intent intent = new Intent(RecordVideoActivity.this, PlayVideoActivity.class);
        intent.putExtra(VIDEO_PATH, mOutputFile.getAbsolutePath());
        startActivityForResult(intent, REQUEST_CODE_TO_PLAY);
        hideRecordController();
    }

    private void hideRecordController() {
        progressBar.setVisibility(GONE);
        btnRecord.setVisibility(GONE);
        ivBack.setVisibility(GONE);
    }

    private void showRecordController() {
        btnRecord.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        Log.d(TAG, "onError what=" + i + " extra=" + i1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TO_PLAY) {
            if (resultCode == RESULT_CANCELED) {
                resetProgress();
                showRecordController();
                deleteFile();
            } else if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("duration", data.getIntExtra("duration", 0));
                intent.putExtra("path", mOutputFile.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private void deleteFile() {
        //放弃该视频
        if (mOutputFile != null && mOutputFile.exists()) {
            Log.i(TAG, "delete unused file:" + mOutputFile.getName() + ",result=" + mOutputFile.delete());
            if (mOutputFile.exists()) {
                Log.i(TAG, "delete unused file:" + mOutputFile.getName() + ",result=" + deleteFile(mOutputFile.getName()));
            }
        }
    }

    /**
     * 重置进度条进度
     */
    private void resetProgress() {
        progressBar.setVisibility(GONE);
        progressBar.setProgress(0);
    }

    /**
     * 释放摄像头资源
     */
    private void releaseMediaRecorder() {
        if (mOutputFile != null && mOutputFile.exists()) {
            if (mOutputFile.length() == 0) {
                Log.i(TAG, "never record,delete cacheFile,result=" + mOutputFile.delete());
            }
        }
        if (mMediaRecorder != null) {
            if (isRecording) {
                mMediaRecorder.stop();
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 初始化View
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        ivBack = findViewById(R.id.ivBack);
        btnRecord = findViewById(R.id.btnRecord);
        progressBar = findViewById(R.id.progressBar);
        surfaceView = findViewById(R.id.surfaceView);
        ivBack.setOnClickListener(this);
        btnRecord.setOnTouchListener(this);
        sp = getSharedPreferences(RecordConfig.RECORD_CONFIG_SP_NAME, MODE_PRIVATE);
    }

    /**
     * 设置进度条显示并调整其大小
     */
    private void setProgressBar() {
        progressBar.setProgress(0);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) progressBar.getLayoutParams();
        params.width = btnRecord.getMeasuredWidth() + 20;
        params.height = btnRecord.getMeasuredHeight() + 20;
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.VISIBLE);
        startTime = System.currentTimeMillis();
        progressHandler = new ProgressHandler();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (progressHandler != null && isRecording) {
                    progressHandler.sendEmptyMessage(0);
                }
            }
        }, 0, 50);
    }

    /**
     * 更新进度条
     */
    class ProgressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                int progress = (int) ((System.currentTimeMillis() - startTime) / (MAX_DURATION / 100));
                if (progress <= 100) {
                    progressBar.setProgress(progress);
                }
            }
        }
    }

    /**
     * 录制按钮触摸事件
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            long downTime=System.currentTimeMillis();
            if (downTime-clickTime>1000){
                if (!isRecording){
                    btnRecord.setPressed(true);
                    RecordTask recordTask = new RecordTask();
                    recordTask.execute();
                }else if (mMediaRecorder!=null){
                    recordComplete();
                }
            }
            clickTime=downTime;
        } else if (action == MotionEvent.ACTION_UP) {
            if (isRecording && mMediaRecorder != null) {
                recordComplete();
            }
        }
        return true;
    }

    /**
     * 开始录制任务
     */
    class RecordTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            mMediaRecorder.start();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isRecording = true;
            setProgressBar();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivBack) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME && isRecording) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}

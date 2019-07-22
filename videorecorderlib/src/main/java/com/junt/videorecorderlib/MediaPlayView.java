package com.junt.videorecorderlib;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;


public class MediaPlayView extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    private final String TAG = MediaPlayView.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private Uri uri;
    private String mediaPath;
    private boolean isLooping;

    public MediaPlayView(Context context) {
        this(context, null);
        Log.i(TAG, "construct()");
    }

    public MediaPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        Log.i(TAG, "construct( , )");

    }

    public MediaPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(TAG, "construct( , , )");
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    /**
     * SurfaceView已经创建，可以初始化MediaPlayer
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreated");
        this.surfaceHolder = surfaceHolder;
        this.surfaceHolder.setSizeFromLayout();
        this.surfaceHolder.setKeepScreenOn(true);
        prepareMediaPlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    /**
     * SurfaceView已经销毁，释放MediaPlayer
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed");
        if (mediaPlayer != null) {
            stop();
            reset();
            mediaPlayer.release();
        }
    }

    /**
     * 初始化MediaPlayer
     */
    public void prepareMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setLooping(isLooping);
            if (!TextUtils.isEmpty(mediaPath)) {
                mediaPlayer.setDataSource(mediaPath);
            } else {
                mediaPlayer.setDataSource(getContext(), uri);
            }
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Media已经可以开始播放
     *
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (onReadyListener != null) {
            onReadyListener.onReady(mp);
        }
    }

    OnReadyListener onReadyListener;

    public interface OnReadyListener {
        void onReady(MediaPlayer mediaPlayer);
    }

    public void setOnReadyListener(OnReadyListener onReadyListener) {
        this.onReadyListener = onReadyListener;
    }

    /**
     * *********************MediaPlayer一些常规操作抽取
     ************************/
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        if (mediaPlayer == null) return false;
        return mediaPlayer.isPlaying();
    }

    public void stop() {
        if (mediaPlayer != null)
        mediaPlayer.stop();
    }

    public void pause() {
        if (mediaPlayer != null)
        mediaPlayer.pause();
    }

    private void reset() {
        if (mediaPlayer != null)
        mediaPlayer.reset();
    }

    public void start() {
        if (mediaPlayer != null)
        mediaPlayer.start();
    }

    public MediaPlayView setLooping(boolean isLooping) {
        this.isLooping = isLooping;
        return this;
    }

    public MediaPlayView setDataSource(@NonNull Context context, @NonNull Uri uri) throws IOException {
        this.uri = uri;
        return this;
    }

    public MediaPlayView setDataSource(String path) throws IOException {
        mediaPath = path;
        return this;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}

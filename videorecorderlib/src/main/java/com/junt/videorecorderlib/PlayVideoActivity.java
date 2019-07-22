package com.junt.videorecorderlib;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.junt.videocompressorlibrary.compress.CompressListener;
import com.junt.videocompressorlibrary.compress.Compressor;
import com.junt.videocompressorlibrary.compress.CustomProgressDialog;
import com.junt.videocompressorlibrary.compress.InitListener;
import com.junt.videocompressorlibrary.exceptions.FFmpegCommandAlreadyRunningException;
import com.junt.videocompressorlibrary.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;

public class PlayVideoActivity extends AppCompatActivity {

    private final String TAG = PlayVideoActivity.class.getSimpleName();
    private MediaPlayView mediaPlayView;
    private String videoPath;
    private Compressor mCompressor;
    private CustomProgressDialog mProcessingDialog;
    private String outPutVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play_video);
        findViewById(R.id.btnGiveUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveUp();
            }
        });

        findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseThisVideo();
            }
        });

        mediaPlayView = findViewById(R.id.playView);
        videoPath = getIntent().getStringExtra(RecordVideoActivity.VIDEO_PATH);
        Log.i(TAG, "videoPath：" + videoPath);
        try {
            mediaPlayView
                    .setDataSource(videoPath)
                    .setLooping(true)
                    .setOnReadyListener(new MediaPlayView.OnReadyListener() {
                        @Override
                        public void onReady(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 放弃该视频
     */
    private void giveUp() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * 选择该视频
     */
    private void chooseThisVideo() {

        File inputFile = new File(videoPath);
        String inputFileName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
        outPutVideoPath = inputFile.getParentFile().getAbsolutePath() + "/" + inputFileName + "_compress.mp4";
        Log.i(TAG, "video compress output path=" + outPutVideoPath);
        startCompress();
    }

    /**
     * 返回压缩结果
     *
     * @param isCompressSuccess
     */
    private void returnResult(String finalVideoPath, boolean isCompressSuccess) {
        Intent intent = new Intent();
        intent.putExtra("path", finalVideoPath);
        intent.putExtra("duration", mediaPlayView.getDuration());
        intent.putExtra("isCompressSuccess", isCompressSuccess);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 视频压缩开始
     */
    private void startCompress() {
        VideoParam videoParam = new VideoParam(videoPath);
        videoParam.obtainVideoParam();
        int videoRotation = videoParam.getVideoRotation();
        int videoWidth = videoParam.getVideoWidth();
        int videoHeight = videoParam.getVideoHeight();
        String videoDuration = videoParam.getDuration();
        double videoLength = Double.parseDouble(videoDuration) / 1000.00;

        try {
            File file = new File(outPutVideoPath);
            if (file.exists()) {
                file.delete();
            }
            String cmd = "";
            Log.i(TAG, "startCompress=mVideoPath=" + videoPath);
            if (videoRotation == 90 || videoRotation == 270) {
                Log.i(TAG, "videoRotation=90");
                cmd = "-y -i " + videoPath + " -strict -2 -vcodec libx264 -preset ultrafast " +
                        "-crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 480x800 -aspect 9:16 " + outPutVideoPath;
            } else {
                Log.i(TAG, "videoRotation=0");
                if (videoWidth > videoHeight) {
                    cmd = "-y -i " + videoPath + " -strict -2 -vcodec libx264 -preset ultrafast " +
                            "-crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 800x480 -aspect 16:9 " + outPutVideoPath;
                } else {
                    cmd = "-y -i " + videoPath + " -strict -2 -vcodec libx264 -preset ultrafast " +
                            "-crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 480x800 -aspect 9:16 " + outPutVideoPath;
                }
            }
            if (mProcessingDialog == null) {
                mProcessingDialog = new CustomProgressDialog(this);
            }
            mProcessingDialog.show();
            mProcessingDialog.setProgress(0);
            execCommand(cmd, videoLength);
        } catch (Exception e) {
            Log.i(TAG, "startCompress=e=" + e.getMessage());
        }
    }

    private void execCommand(final String cmd, final double videoLength) throws FFmpegCommandAlreadyRunningException, FFmpegNotSupportedException {
        Log.i(TAG, "cmd= " + cmd);
        if (mCompressor == null) {
            mCompressor = new Compressor(this);
        }
        mCompressor.loadBinary(new InitListener() {
            @Override
            public void onLoadSuccess() {
                Log.v(TAG, "load library succeed");
            }

            @Override
            public void onLoadFail(String reason) {
                Log.i(TAG, "load library fail:" + reason);
            }
        });
        mCompressor.execCommand(cmd, new CompressListener() {
            @Override
            public void onExecSuccess(String message) {
                mProcessingDialog.dismiss();
                Log.i(TAG, "video compress successfully ");
                returnResult(outPutVideoPath, true);
            }

            @Override
            public void onExecFail(String reason) {
                Log.i(TAG, "fail " + reason);
                mProcessingDialog.dismiss();
                returnResult(videoPath, false);
            }

            @Override
            public void onExecProgress(String message) {
                try {
                    Log.i(TAG, "progress " + message);
                    double switchNum = mCompressor.getProgress(message, videoLength);
                    if (switchNum == 10000) {
                        //如果找不到压缩的片段，返回为10000
                        Log.i(TAG, "10000");
                        mProcessingDialog.setProgress(0);
                    } else {
                        mProcessingDialog.setProgress((int) (mCompressor.getProgress(message, videoLength) / 10));
                    }
                } catch (Exception e) {
                    mProcessingDialog.dismiss();
                    Log.i(TAG, "e=" + e.getMessage());
                    returnResult(videoPath, false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        giveUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCompressor();
    }

    private void releaseCompressor() {
        if (mCompressor != null) {
            mCompressor.destory();
        }
    }
}

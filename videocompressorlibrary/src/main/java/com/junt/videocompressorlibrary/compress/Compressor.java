package com.junt.videocompressorlibrary.compress;

import android.app.Activity;
import android.util.Log;

import com.junt.videocompressorlibrary.ExecuteBinaryResponseHandler;
import com.junt.videocompressorlibrary.FFmpeg;
import com.junt.videocompressorlibrary.LoadBinaryResponseHandler;
import com.junt.videocompressorlibrary.exceptions.FFmpegCommandAlreadyRunningException;
import com.junt.videocompressorlibrary.exceptions.FFmpegNotSupportedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  @dec  
 *  @author apeng
 *  @date  2018/10/31 11:44
 */
public class Compressor {
    protected String TAG = "COMPRESSOR";//日志输出标志
    public Activity a;
    public FFmpeg ffmpeg;

    public Compressor(Activity activity) {
        a = activity;
        ffmpeg = FFmpeg.getInstance(a);
    }

    public void loadBinary(final InitListener mListener) throws FFmpegNotSupportedException {
        ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFailure() {
                mListener.onLoadFail("incompatible with this device");
            }

            @Override
            public void onSuccess() {
                mListener.onLoadSuccess();
            }

            @Override
            public void onFinish() {

            }
        });
    }

    public void execCommand(String cmd, final CompressListener mListener) throws FFmpegCommandAlreadyRunningException {
        String[] cmds = cmd.split(" ");
        ffmpeg.execute(cmds, new ExecuteBinaryResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(String message) {
                mListener.onExecProgress(message);
            }

            @Override
            public void onFailure(String message) {
                mListener.onExecFail(message);
            }

            @Override
            public void onSuccess(String message) {
                mListener.onExecSuccess(message);
            }

            @Override
            public void onFinish() {
            }
        });
    }

    public double getProgress(String source,double videoLength) {
        double progress=0;
        if (source.contains("too large")) {//当文件过大的时候，会会出现 Past duration x.y too large
            Log.i(TAG, "too large");
            return progress;
        }
        Pattern p = Pattern.compile("00:\\d{2}:\\d{2}");
        Matcher m = p.matcher(source);
        if (m.find()) {
            //00:00:00
            String result = m.group(0);
            String temp[] = result.split(":");
            double seconds = Double.parseDouble(temp[1]) * 60 + Double.parseDouble(temp[2]);
            if (0 != videoLength) {
                return seconds / videoLength * 1000;
            }
            if (seconds == videoLength) {
                return 1000;
            }
        }
//        MyLog.i(TAG, "!m.find()="+getProgressNum);
        return 10000;//出现异常的时候，返回为10000
//      return 0;//出现异常的时候，显示上一个进度条
    }



    public void destory() {
        if (ffmpeg != null) {
            if (ffmpeg.isFFmpegCommandRunning()) {
                Log.i(TAG, "killRunningProcesses");
                ffmpeg.killRunningProcesses();
                Log.i(TAG, "killProcesses");
            }
        }
    }

}

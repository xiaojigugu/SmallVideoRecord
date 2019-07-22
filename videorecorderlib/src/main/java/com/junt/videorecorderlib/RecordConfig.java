package com.junt.videorecorderlib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Environment;

@SuppressLint("ApplySharedPref")
public class RecordConfig {
    private static RecordConfig recordConfig;
    private Activity activity;

    public static final String RECORD_CONFIG_SP_NAME = "RECORD_CONFIG";
    private SharedPreferences sp;

    static final String CONFIG_QUALITY = "quality";
    static final String CONFIG_FOCUS_MODE = "focus_mode";
    static final String CONFIG_ENCODING_BIT_RATE = "bitRate";
    static final String CONFIG_FRAME_RATE = "frameRate";
    static final String CONFIG_OUTPUT_PATH = "outputPath";
    static final String CONFIG_MAX_DURATION = "duration";

    public static RecordConfig getInstance() {
        if (recordConfig == null) {
            recordConfig = new RecordConfig();
        }
        return recordConfig;
    }

    public RecordConfig with(Activity activity) {
        this.activity = activity;
        sp = activity.getSharedPreferences(RECORD_CONFIG_SP_NAME, Context.MODE_PRIVATE);
        return this;
    }

    public RecordConfig setQuality(int quality) {
        if (sp == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        //default：480P
        sp.edit().putInt(CONFIG_QUALITY, quality).commit();
        return this;
    }

    public RecordConfig setFocusMode(String focusMode) {
        if (sp == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        //default:FOCUS_MODE_CONTINUOUS_VIDEO
        sp.edit().putString(CONFIG_FOCUS_MODE, focusMode).commit();
        return this;
    }

    public RecordConfig setEncodingBitRate(int encodingBitRate) {
        if (sp == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        //default:5 * 1280 * 720
        sp.edit().putInt(CONFIG_ENCODING_BIT_RATE, encodingBitRate).commit();
        return this;
    }

    public RecordConfig setFrameRate(int frameRate) {
        if (sp == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        //default:20
        sp.edit().putInt(CONFIG_FRAME_RATE, frameRate).commit();
        return this;
    }

    /**
     * @param outputPath 视频输出路径默认前缀为 /storage/emulated/0
     *                   eg：outputPath="/smallvideo/files/VID_timestamp.mp4",最终的文件路径为/storage/emulated/0/smallvideo/files/VID_timestamp.mp4
     * @return RecordConfig
     */
    public RecordConfig setOutputPath(String outputPath) {
        if (sp == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        //default：/storage/emulated/0/junt/video/VID_timestamp.mp4
        String path = Environment.getExternalStorageDirectory() + outputPath;
        sp.edit().putString(CONFIG_OUTPUT_PATH, path).commit();
        return this;
    }

    public RecordConfig setMaxDuration(int duration) {
        if (sp == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        //default:6*1000ms
        sp.edit().putInt(CONFIG_MAX_DURATION, duration).commit();
        return this;
    }

    public static String obtainVideoPath(Intent data){
        if (data==null){
            throw new NullPointerException("data is NULL");
        }
        return data.getStringExtra("path");
    }

    public static String obtainVideoDuration(Intent data){
        if (data==null){
            throw new NullPointerException("data is NULL");
        }
        return data.getStringExtra("duration");
    }

    public void obtainVideo( int requestCode) {
        if (activity == null) {
            throw new NullPointerException("Please innovate method 'with()' first");
        }
        activity.startActivityForResult(new Intent(activity, RecordVideoActivity.class), requestCode);
    }

    public static class Quality {
        public static int QUALITY_LOW = CamcorderProfile.QUALITY_LOW;
        public static int QUALITY_HIGH = CamcorderProfile.QUALITY_HIGH;
        public static int QUALITY_QCIF = CamcorderProfile.QUALITY_QCIF;
        public static int QUALITY_CIF = CamcorderProfile.QUALITY_CIF;
        public static int QUALITY_480P = CamcorderProfile.QUALITY_480P;
        public static int QUALITY_720P = CamcorderProfile.QUALITY_720P;
        public static int QUALITY_1080P = CamcorderProfile.QUALITY_1080P;
        public static int QUALITY_QVGA = CamcorderProfile.QUALITY_QVGA;
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public static int QUALITY_2160P = CamcorderProfile.QUALITY_2160P;
    }

    public static class FocusMode {
        public static String FOCUS_MODE_AUTO = Camera.Parameters.FOCUS_MODE_AUTO;
        public static String FOCUS_MODE_CONTINUOUS_PICTURE = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        public static String FOCUS_MODE_CONTINUOUS_VIDEO = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        public static String FOCUS_MODE_EDOF = Camera.Parameters.FOCUS_MODE_EDOF;
        public static String FOCUS_MODE_INFINITY = Camera.Parameters.FOCUS_MODE_INFINITY;
        public static String FOCUS_MODE_FIXED = Camera.Parameters.FOCUS_MODE_FIXED;
        public static String FOCUS_MODE_MACRO = Camera.Parameters.FOCUS_MODE_MACRO;
    }
}

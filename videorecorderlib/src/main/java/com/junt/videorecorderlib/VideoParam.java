package com.junt.videorecorderlib;

import android.media.MediaMetadataRetriever;
import android.util.Log;

public class VideoParam {
    private final String TAG= VideoParam.class.getSimpleName();

    private String videoPath;
    private String duration;
    private int  videoHeight, videoWidth,videoRotation;
    private double length;

    public String getTAG() {
        return TAG;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoRotation() {
        return videoRotation;
    }

    public void setVideoRotation(int videoRotation) {
        this.videoRotation = videoRotation;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public VideoParam(String videoPath) {
        this.videoPath = videoPath;
    }

    public VideoParam obtainVideoParam(){
        try {
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            retr.setDataSource(videoPath);
            duration = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);//获取视频时长
            videoWidth = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));//获取视频的宽度
            videoHeight = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));//获取视频的高度
            videoRotation = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));//获取视频的角度

            Log.i(TAG, "videoWidth=" + videoWidth);
            Log.i(TAG, "videoHeight=" + videoHeight);

            length = Double.parseDouble(duration) / 1000.00;
            Log.i(TAG, "videoLength=" + length);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "e=" + e.getMessage());
            length = 0.00;
        }
        return this;
    }
}

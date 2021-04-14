# SmallVideoRecord
短视频录制

此库已迁移，最新地址详见：https://gitee.com/giteeguguji/SmallVideoRecord

```java
//开启录制
RecordConfig
    .getInstance()
    .with(this)
    //设置录制视频质量
    .setQuality(RecordConfig.Quality.QUALITY_720P)
    //比特率，比特率越大，画面越清晰，但是文件体积会快速增长
    .setEncodingBitRate(1_000_000)
    //帧率
    .setFrameRate(20)
    //最大录制时长
    .setMaxDuration(6*1000)
    //对焦模式
    .setFocusMode(RecordConfig.FocusMode.FOCUS_MODE_CONTINUOUS_VIDEO)
    //适配AndroidQ，完整的路径为/data/data/包名/files/smallvideo/
    .setOutputPath("/smallvideo/")
    //跳转录制
    .obtainVideo(REQUEST_CODE_VIDEO);

//接收视频路径及时长信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_VIDEO&&resultCode==RESULT_OK){
            String videoPath=RecordConfig.obtainVideoPath(data);
            int duration=RecordConfig.obtainVideoDuration(data);
            Log.i(this.getClass().getSimpleName(),"obtainVideoPath="+videoPath+" duration="+generateTime(duration));
        }
    }
```


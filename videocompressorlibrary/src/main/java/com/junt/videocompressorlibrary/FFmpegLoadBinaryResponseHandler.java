package com.junt.videocompressorlibrary;

public interface FFmpegLoadBinaryResponseHandler extends ResponseHandler {

    /**
     * on Fail
     */
    public void onFailure();

    /**
     * on Success
     */
    public void onSuccess();

}

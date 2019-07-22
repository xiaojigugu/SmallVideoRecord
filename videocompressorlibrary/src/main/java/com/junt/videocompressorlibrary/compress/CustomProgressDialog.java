package com.junt.videocompressorlibrary.compress;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.junt.videocompressorlibrary.R;

import java.util.Locale;

public class CustomProgressDialog extends Dialog {

    private TextView tvMessage, tvProgress;
    private ProgressBar progressBar;

    public CustomProgressDialog(Context context) {
        super(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_progress);
        tvMessage = findViewById(R.id.tvMessage);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
    }

    public void setMessage(String message){
        tvMessage.setText(message);
    }

    public void setProgress(int progress) {
        tvProgress.setText(String.format(Locale.CHINA, "%d%s", progress, "%"));
        progressBar.setProgress(progress);
    }
}

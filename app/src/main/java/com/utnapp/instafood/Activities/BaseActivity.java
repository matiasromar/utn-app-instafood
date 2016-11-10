package com.utnapp.instafood.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.R;

public abstract class BaseActivity extends AppCompatActivity {

    private ProgressDialog progress;

    protected void finishActivityWithError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BaseActivity.this.finish();
            }
        }, CommonUtilities.WAIT_LENGTH_LONG);
    }

    protected void showLoadingIcon() {
        if(progress == null){
            progress = new ProgressDialog(this);
        }
        progress.setTitle("");
        progress.setMessage(getString(R.string.loadingMsg));
        progress.show();
    }

    protected void hideLoadingIcon() {
        if(progress != null){
            progress.dismiss();
        }
    }
}

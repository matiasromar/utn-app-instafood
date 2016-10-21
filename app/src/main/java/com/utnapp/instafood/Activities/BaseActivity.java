package com.utnapp.instafood.Activities;

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

    private final Boolean internetRequired;

    protected BaseActivity(Boolean internetRequired) {
        super();
        this.internetRequired = internetRequired;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!CommonUtilities.isNetworkAvailable(this) && this.internetRequired) {
            showNoConnectionErrorAndFinish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(connectionLostReceiver, new IntentFilter(getString(R.string.custom_item_filter_internet_lost)));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(connectionLostReceiver);

        super.onStop();
    }

    BroadcastReceiver connectionLostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean internetConnectionRequired = internetRequired;

            if(internetConnectionRequired){
                showNoConnectionErrorAndFinish();
            }
        }
    };

    private void showNoConnectionErrorAndFinish() {
        finishActivityWithError(getString(R.string.internet_connection_needed));
    }

    protected void finishActivityWithError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BaseActivity.this.finish();
            }
        }, CommonUtilities.WAIT_LENGTH_LONG);
    }
}

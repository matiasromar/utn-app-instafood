package com.utnapp.instafood.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.R;

public class InternetConnectivityReceiver extends BroadcastReceiver {
    public InternetConnectivityReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!CommonUtilities.isNetworkAvailable(context)){
            context.sendBroadcast(new Intent(context.getString(R.string.custom_item_filter_internet_lost)));
        }
    }
}

package com.buahbatu.toyotasalesman.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.buahbatu.toyotasalesman.AppConfig;
import com.buahbatu.toyotasalesman.MainActivity;

/**
 * Created by maaakbar on 2/13/16.
 */
public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppConfig.saveOnTracked(context, false);
        AppConfig.saveLoginStatus(context, false);
        Intent start = new Intent(context, MainActivity.class);
        context.startActivity(start);
    }
}

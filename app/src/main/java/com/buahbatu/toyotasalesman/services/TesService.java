package com.buahbatu.toyotasalesman.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by maaakbar on 2/13/16.
 */
public class TesService extends IntentService {
    public TesService() {
        super("TesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent!=null){
            Log.i("TEST Service", "onHandleIntent "+intent.getStringExtra("data"));
        }
    }
}

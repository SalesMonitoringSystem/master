package com.buahbatu.toyotasalesman.network;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created by maaakbar on 2/1/16.
 */
public class GetWebTask extends AsyncTask<HttpEntity, Void, String> {
    private Context context;
    private URL url;
    private final String TAG = "GET web";

    private boolean isNetworkAvailable() {
        String permission = "android.permission.INTERNET";
        int res = context.checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED){
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null;
        }
        return false;
    }

    public GetWebTask(Context context, URL url) {
        this.context = context;
        this.url = url;
    }

    @Override
    protected String doInBackground(HttpEntity... params) {
        HttpEntity reqEntity = params[0];
        StringBuilder response = new StringBuilder("");
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("User-Agent", "Android");
                con.setRequestProperty("Connection", "Keep-Alive");

                int status = con.getResponseCode();
                Log.i(TAG, "response code: " + Integer.toString(status));

                //Get Response
                //InputStream is = con.getInputStream();
                InputStream is;
                if (status >= HttpStatus.SC_BAD_REQUEST)
                    is = con.getErrorStream();
                else
                    is = con.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();

                //if(con != null) {
                con.disconnect();
                //}
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error internet connection ");
            }
            Log.i(TAG, "response-" + response.toString());
        }else response = new StringBuilder("No network available!");
        return response.toString();
    }
}

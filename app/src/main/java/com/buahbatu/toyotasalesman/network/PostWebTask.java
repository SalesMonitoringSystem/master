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

public class PostWebTask extends AsyncTask<HttpEntity, Void, String> {
    private Context context;
    private URL url;
    private HttpConnectionEvent httpConnectionEvent;
    private final String TAG = "POST web";

    public PostWebTask(Context context, URL url) {
        this.context = context;
        this.url = url;
    }

    public PostWebTask(Context context, URL url, HttpConnectionEvent event) {
        this.context = context;
        this.url = url;
        this.httpConnectionEvent = event;
    }

    @Override
    protected String doInBackground(HttpEntity... params) {
        HttpEntity reqEntity = params[0];
        StringBuilder response = new StringBuilder("");
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(10000);
                con.setConnectTimeout(15000);
                // optional default is GET
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);

                con.setRequestProperty("User-Agent", "Android");
                con.setRequestProperty("Connection", "Keep-Alive");
//            con.setRequestProperty("Cache-Control", "no-cache");
                con.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());
                Log.i(TAG, reqEntity.getContentType().getName() + " : " + reqEntity.getContentType().getValue());
//            con.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + MultiPartFormOutputStream.createBoundary());

                con.connect();

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        con.getOutputStream());

                reqEntity.writeTo(wr);
                //writOutValue(params[0], wr);

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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (httpConnectionEvent != null)
            httpConnectionEvent.preEvent();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (httpConnectionEvent != null)
            httpConnectionEvent.postEvent(s);
    }

    public interface HttpConnectionEvent {
        void preEvent();
        void postEvent(String... result);
    }
}
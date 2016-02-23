package com.buahbatu.toyotasalesman.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.buahbatu.toyotasalesman.AppConfig;
import com.buahbatu.toyotasalesman.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;

/**
 * This is a helper in network communication
 */
public class NetHelper {
    final static String TAG = "NetHelper";
    final static String domainServer = "SMSDomain";

    public static void storeDomainAddress(Context context, String domain){
        SharedPreferences preferences = AppConfig.getDefaultPreferences(context);
        preferences.edit().putString(domainServer, domain).apply();
    }

    static String getDomainAddress(Context context){
        SharedPreferences preferences = AppConfig.getDefaultPreferences(context);
        return context.getString(R.string.default_protocol)+preferences.getString(domainServer, context.getString(R.string.default_domain));
    }

    static String getReportDomainAddress(Context context){
        return getDomainAddress(context) + context.getString(R.string.report_url);
    }

    static String getLoginDomainAddress(Context context){
        return getDomainAddress(context) + context.getString(R.string.login_url);
    }

    static boolean isDefaultAccount(Context context, String username, String password){
        return  username.equals(context.getString(R.string.default_user))
                && password.equals(context.getString(R.string.default_pass));
    }

    public static void login(Context context, String username, String password,
                             PostWebTask.HttpConnectionEvent connectionEvent){
        try {
            PostWebTask postWebTask = new PostWebTask(context, new URL(getLoginDomainAddress(context)), connectionEvent);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody(context.getString(R.string.api_username), username);
            entityBuilder.addTextBody(context.getString(R.string.api_password), password);
            entityBuilder.addTextBody(context.getString(R.string.api_imei),
                isDefaultAccount(context, username, password) ? context.getString(R.string.default_imei) : AppConfig.getImeiNum(context));
            Log.i(TAG, "?" + context.getString(R.string.api_username) + "=" + username + "&" + context.getString(R.string.api_password) + "=" + password + "&" + context.getString(R.string.api_imei) + "=" + AppConfig.getImeiNum(context));
            postWebTask.execute(entityBuilder.build());
        }catch (MalformedURLException e){
            e.printStackTrace();
            Log.i(TAG, "Error domain");
        }

    }

    public static void report(Context context, String username, double lat, double longi, String street,
                              PostWebTask.HttpConnectionEvent connectionEvent){
        Log.i(TAG, "report");
        try {
            PostWebTask postWebTask = new PostWebTask(context, new URL(getReportDomainAddress(context)), connectionEvent);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody(context.getString(R.string.api_username), username);
            entityBuilder.addTextBody(context.getString(R.string.api_coordinate), lat+","+longi);
            entityBuilder.addTextBody(context.getString(R.string.api_alamat), street);
            Log.i(TAG, "?" + context.getString(R.string.api_username) + "=" + username
                    + "&" + context.getString(R.string.api_coordinate) + "=" + lat + "," + longi
                    + "&" + context.getString(R.string.api_alamat) + "=" + street);
            postWebTask.execute(entityBuilder.build());
        }catch (MalformedURLException e){
            e.printStackTrace();
            Log.i(TAG, "Error domain");
        }
    }

    public static String getLoginResponse(String result) throws JSONException{
        JSONArray object = new JSONArray(result);
        return object.get(0).toString();
        // return "user";
    }

    public static int getNextUpdateSchedule(String result) throws JSONException{
        JSONObject object = new JSONObject(result);
        switch (object.getString("status")){
            case "success": return 60;
            case "failed": return object.getInt("next");
        }
        return 60;
    }
}

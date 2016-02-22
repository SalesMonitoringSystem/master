package com.buahbatu.toyotasalesman;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

/**
 * This is for configuration in app
 */
public class AppConfig {
    private static String preference = "SMS_pref";
    private static String loginKey = "login";
    private static String onTrackedKey = "bind";
    public static String versionKey = "version";

    public static SharedPreferences getDefaultPreferences(Context context){
        return context.getSharedPreferences(preference, Context.MODE_PRIVATE);
    }

    public static void saveLoginStatus(Context context, boolean isLoggedIn){
        getDefaultPreferences(context).edit().putBoolean(loginKey, isLoggedIn).apply();
    }

    public static boolean getLoginStatus(Context context){
        return getDefaultPreferences(context).getBoolean(loginKey, false);
    }

    public static void saveOnTracked(Context context, boolean onTracked){
        getDefaultPreferences(context).edit().putBoolean(onTrackedKey, onTracked).apply();
    }

    public static boolean getOnTracked(Context context){
        return getDefaultPreferences(context).getBoolean(onTrackedKey, false);
    }

    public static String getImeiNum(Context context){
        TelephonyManager mngr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return mngr.getDeviceId();
//        return "359734050001172";
    }

    public static void storeAccount(Context context, String username, String pass){
        SharedPreferences.Editor editor = getDefaultPreferences(context).edit();
        editor.putString(context.getString(R.string.api_username), username);
        editor.putString(context.getString(R.string.api_password), pass);
        editor.apply();
    }

    public static String getUserName(Context context){
        return getDefaultPreferences(context).getString(context.getString(R.string.api_username), "User");
    }

    public static int getLocalAppVersion(Context context){
        return context.getResources().getInteger(R.integer.version);
    }
}

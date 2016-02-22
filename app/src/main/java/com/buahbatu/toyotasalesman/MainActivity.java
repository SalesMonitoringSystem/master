package com.buahbatu.toyotasalesman;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.buahbatu.toyotasalesman.network.NetHelper;
import com.buahbatu.toyotasalesman.network.PostWebTask;
import com.buahbatu.toyotasalesman.services.ReportingService;
import org.json.JSONException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public final static int requestPhonePermission = 12;
    final String TAG = "MainActivity";

    // UI
    TextView userName;
    TextView pass;
    Button loginButton;

    ReportingService reportingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        userName = (TextView) findViewById(R.id.username_text);
        pass = (TextView) findViewById(R.id.pass_text);
        loginButton = (Button) findViewById(R.id.login_button);
        setUI(AppConfig.getLoginStatus(MainActivity.this));
        loginButton.setOnClickListener(this);
        findViewById(R.id.logo_view).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, ReportingService.class);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            Log.i(TAG, "onServiceConnected ");
            ReportingService.LocalBinder b = (ReportingService.LocalBinder) binder;
            reportingService = b.getService();
            reportingService.setUpGoogleClient(MainActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            reportingService = null;
        }
    };

    // update UI
    void setUI(boolean isLoogedIn){
        if (isLoogedIn) {
            userName.setVisibility(View.GONE);
            pass.setVisibility(View.GONE);
            loginButton.setText(R.string.logout);
        }else {
            userName.setVisibility(View.VISIBLE);
            pass.setVisibility(View.VISIBLE);
            loginButton.setText(R.string.login);
        }
    }

    boolean checkForPermission(){
        return checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED;
    }


    void askForPermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
        }, requestPhonePermission);
    }

    void doLogin(){
        if (!userName.getText().toString().isEmpty()){
            if (!pass.getText().toString().isEmpty()){
                if (!reportingService.mGoogleApiClient.isConnected())
                    reportingService.mGoogleApiClient.connect();
                else
                    NetHelper.login(this, userName.getText().toString(), pass.getText().toString(), loginEvent);
            }else Toast.makeText(this, "Please fill your password", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this, "Please fill your username", Toast.LENGTH_SHORT).show();
    }

    ProgressDialog progressDialog;
    PostWebTask.HttpConnectionEvent loginEvent = new PostWebTask.HttpConnectionEvent() {
        @Override
        public void preEvent() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Logging In");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public void postEvent(String... result) { // check login state
            progressDialog.dismiss();
            // handle if login fail and success
            try {
                String response = NetHelper.getLoginResponse(result[0]);
//                String response = "imei";
                switch (response){
                    case "success":
                        AppConfig.saveLoginStatus(MainActivity.this, true);
                        AppConfig.storeAccount(MainActivity.this, userName.getText().toString(), pass.getText().toString());

                        // update UI
                        setUI(true);

                        // start tracking
                        Intent mServiceIntent = new Intent(MainActivity.this, ReportingService.class);
                        mServiceIntent.putExtra("run", true);
                        startService(mServiceIntent);
                        break;
                    case "username":
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Username is not registered")
                                .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;
                    case "password":
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Your password is not match")
                                .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;
                    case "imei":
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Your phone is not registered")
                                .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;
                    case "time":
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("It is out of working hours")
                                .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;
                    default:
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Something wrong occurred, please contact your administrator")
                                .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;

                }
            }catch (JSONException e){
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Something wrong occurred, please contact your administrator")
                        .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    };

    void doLogout(){
        AppConfig.saveLoginStatus(this, false);
        AppConfig.storeAccount(this, "", "");

        // update UI
        setUI(false);

        // stop tracking
        Intent mServiceIntent = new Intent(MainActivity.this, ReportingService.class);
        mServiceIntent.putExtra("stop", true);
        startService(mServiceIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                if (checkForPermission()) {
                    switch (((Button) v).getText().toString()) {
                        case "Login":
                            doLogin();
                            break;
                        case "Logout":
                            doLogout();
                            break;
                    }
                } else askForPermission();
                break;
            case R.id.logo_view:
                new AlertDialog.Builder(MainActivity.this).setTitle("Phone IMEI")
                        .setMessage("Your imei " + AppConfig.getImeiNum(MainActivity.this))
                        .setPositiveButton("Ok got it!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case requestPhonePermission:
                boolean granted = true;
                for (int result:grantResults) {
                    if (result==PackageManager.PERMISSION_DENIED) {
                        granted = false;
                        break;
                    }
                }
                if (granted) doLogin(); break;
            default: Toast.makeText(this, "Please give permissions", Toast.LENGTH_SHORT).show(); break;
        }
    }
}

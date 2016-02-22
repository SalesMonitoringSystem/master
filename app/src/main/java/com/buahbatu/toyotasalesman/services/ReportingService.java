package com.buahbatu.toyotasalesman.services;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.buahbatu.toyotasalesman.AppConfig;
import com.buahbatu.toyotasalesman.MainActivity;
import com.buahbatu.toyotasalesman.R;
import com.buahbatu.toyotasalesman.network.NetHelper;
import com.buahbatu.toyotasalesman.network.PostWebTask;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by maaakbar on 2/12/16.
 */
public class ReportingService extends Service implements LocationListener {
    public final static int NOTIFICATION_ID = 2;
    final String TAG = "Reporting Service";
    private final IBinder mBinder = new LocalBinder();

    public GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    // status
    boolean running;

    public class LocalBinder extends Binder {
        public ReportingService getService() {
            return ReportingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationRequest = createLocationRequest();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent!=null){
            if (intent.getBooleanExtra("run", false)){
                Log.i(TAG, "onStartCommand ");
                AppConfig.saveOnTracked(this, true);
                showNotification();
                startLocationUpdates();
            }else if (intent.getBooleanExtra("stop", false)){
                Log.i(TAG, "onStartStop");
                AppConfig.saveOnTracked(this, false);
                dismissNotification();
                stopLocationUpdates();
            }
        }
        return START_STICKY;
    }

    public void setUpGoogleClient(final Activity activity){
        if (mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Log.i(TAG, "API GOOGLE onConnected");
                            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                    .addLocationRequest(mLocationRequest);
                            PendingResult<LocationSettingsResult> result =
                                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                                            builder.build());

                            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                                @Override
                                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                                    final Status status = locationSettingsResult.getStatus();
                                    // final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                                    switch (status.getStatusCode()) {
                                        case LocationSettingsStatusCodes.SUCCESS:
                                            // All location settings are satisfied. The client can
                                            // initialize location requests here.
                                            // ...
                                            break;
                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                            // Location settings are not satisfied, but this can be fixed
                                            // by showing the user a dialog.
                                            try {
                                                // Show the dialog by calling startResolutionForResult(),
                                                // and check the result in onActivityResult().
                                                status.startResolutionForResult(activity, MainActivity.requestPhonePermission);
                                            } catch (IntentSender.SendIntentException e) {
                                                // Ignore the error.
                                            } catch (Exception e) {
                                                Log.e(TAG, "onResult ERROR");
                                                e.printStackTrace();
                                            }
                                            break;
                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                            // Location settings are not satisfied. However, we have no way
                                            // to fix the settings so we won't show the dialog.
                                            // ...
                                            break;
                                    }
                                }
                            });
                        }
                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .build();
        }
    }

    boolean showNotification(){
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)   // the status icon
                .setTicker(this.getString(R.string.ticker))        // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(this.getString(R.string.app_name))  // the label of the entry
                .setContentText(this.getString(R.string.tracked))            // the contents of the entry
                .setContentIntent(contentIntent)      // The intent to send when the entry is clicked
                .build();
        startForeground(NOTIFICATION_ID, notification);
        return true;
    }

    boolean dismissNotification(){
        // update UI
        stopForeground(true);
        return false;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    boolean checkPermission(){
        String location_fine = "android.permission.ACCESS_FINE_LOCATION";
        String location_coarse = "android.permission.ACCESS_COARSE_LOCATION";
        int permission_fine = checkCallingOrSelfPermission(location_fine);
        int permission_coarse = checkCallingOrSelfPermission(location_coarse);
        return permission_fine == PackageManager.PERMISSION_GRANTED && permission_coarse == PackageManager.PERMISSION_GRANTED;
    }

    public void startLocationUpdates() {
        Log.i(TAG, "startTracking.1");
        if (mGoogleApiClient!=null) {
            if (checkPermission()) {
                Log.i(TAG, "startTracking");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else Log.i(TAG, "Permission needed");
        }
    }

    public void stopLocationUpdates() {
        Log.i(TAG, "stopTracking.1");
        if (mGoogleApiClient!=null) {
            Log.i(TAG, "stopTracking");
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private LocationRequest createLocationRequest(){
        LocationRequest mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(2000);
        mLocationRequest.setInterval(getResources().getInteger(R.integer.interval) /*second*/ * 1000 /*millis*/); // in millis
        mLocationRequest.setFastestInterval(getResources().getInteger(R.integer.fast_interval) /*second*/ * 1000 /*millis*/); // in millis
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged "+location.getLongitude());

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        String street = "Unknown";
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knowName = addresses.get(0).getFeatureName();
                street = address + " " + city + " " + state + " " + country + " " + postalCode + " " + knowName;
                Log.i(TAG, "street "+street);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        NetHelper.report(this, AppConfig.getUserName(this), location.getLatitude(),
                location.getLongitude(), street, new PostWebTask.HttpConnectionEvent() {
            @Override
            public void preEvent() {

            }

            @Override
            public void postEvent(String... result) {

            }
        });
    }
}

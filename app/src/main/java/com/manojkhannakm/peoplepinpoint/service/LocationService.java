package com.manojkhannakm.peoplepinpoint.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.appspot.people_pinpoint.myApi.MyApi;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.manojkhannakm.peoplepinpoint.Constants;
import com.manojkhannakm.peoplepinpoint.entity.PersonEntity;

import java.io.IOException;

/**
 * @author Manoj Khanna
 */

public class LocationService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String EXTRA_LOCAL_PERSON_ENTITY = "localPersonEntity";
    public static final long REQUEST_INTERVAL = 10 * 60 * 1000;
    public static final long REQUEST_FAST_INTERVAL = 5 * 60 * 1000;

    private PersonEntity mLocalPersonEntity;
    private GoogleApiClient mGoogleApiClient;
    private MyApi mMyApi;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocalPersonEntity = intent.getParcelableExtra(EXTRA_LOCAL_PERSON_ENTITY);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mMyApi = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                .setRootUrl(Constants.API_URL)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(REQUEST_INTERVAL);
        locationRequest.setFastestInterval(REQUEST_FAST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //noinspection ResourceType
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(LocationService.class.getName(), result.toString());
    }

    @Override
    public void onLocationChanged(final Location location) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mMyApi.updatePersonLocation(Constants.ANDROID_CLIENT_ID, mLocalPersonEntity.getId(),
                            location.getLatitude(), location.getLongitude(), System.currentTimeMillis())
                            .execute();
                } catch (IOException e) {
                    Log.e(LocationService.class.getName(), e.getMessage(), e);
                }
            }

        }).start();
    }

}

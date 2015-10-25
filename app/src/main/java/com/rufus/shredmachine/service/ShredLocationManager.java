package com.rufus.shredmachine.service;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rufus.shredmachine.ShredMachineApplication;
import com.rufus.shredmachine.model.GPSData;
import com.rufus.shredmachine.model.TrackResult;
import com.rufus.shredmachine.utils.Constants;
import com.rufus.shredmachine.utils.TimeUtil;

import timber.log.Timber;

public class ShredLocationManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    TrackingService mTrackingService;

    Location mCurrentLocation;
    TrackResult trackResult;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    private static ShredLocationManager mInstance = null;

    private ShredLocationManager(TrackingService trackingService) {
        mTrackingService = trackingService;
        buildGoogleApiClient();
        trackResult = new TrackResult();
        trackResult.startTime = TimeUtil.getTimeStamp();
        trackResult.save();
        ShredMachineApplication.getDefaultSharePreferences().edit().putLong(Constants.SHARE_PREFERENCE.ACTIVE_TRACK_ID, trackResult.id).apply();
        Timber.i("trackResult id: %d", trackResult.id);
    }

    //Singleton
    public static ShredLocationManager getInstance(TrackingService trackingService) {
        if (mInstance == null) {
            mInstance = new ShredLocationManager(trackingService);
        }
        return mInstance;
    }

    public synchronized void buildGoogleApiClient() {
        Timber.i("Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(mTrackingService)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    public void disconnectGoogleApiClient() {
        mGoogleApiClient.disconnect();
    }

    public void connectGoogleApiClient() {
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void destroyLocationService() {
        if (mGoogleApiClient.isConnected()) {
            Timber.i("destroying LocationService");
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        if (mGoogleApiClient != null)
            mGoogleApiClient = null;
        Timber.i("destroying LocationManager");
        mInstance = null;
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle bundle) {
        Timber.i("Connected to GoogleApiClient");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Timber.i("Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        //notify the UI to draw line on map
        ShredMachineApplication.getDefaultEvent().post(new LocationUpdateEvent(location));
        //store it into db
        GPSData gpsData = new GPSData(location);
        gpsData.associateTrackResult(trackResult);
        gpsData.save();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Timber.i("Connection failed: ConnectionResult.getErrorCode() = %s", result.getErrorCode());
    }
}

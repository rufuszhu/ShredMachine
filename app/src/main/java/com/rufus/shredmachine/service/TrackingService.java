package com.rufus.shredmachine.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.rufus.shredmachine.R;
import com.rufus.shredmachine.activity.MainActivity;
import com.rufus.shredmachine.bean.GPSData;
import com.rufus.shredmachine.bean.TrackResult;
import com.rufus.shredmachine.utils.Constants;
import com.rufus.shredmachine.utils.GlobalApplication;
import com.rufus.shredmachine.utils.TimeUtil;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class TrackingService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private NotificationManager mNotificationManager;

    protected Notification pauseNotification;
    protected Notification playNotification;

    protected Location mCurrentLocation;

    private TrackResult trackResult;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    @Override
    public void onCreate() {
        super.onCreate();
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent playIntent = new Intent(this, TrackingService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent pauseIntent = new Intent(this, TrackingService.class);
        pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
        PendingIntent pPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);


        playNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Shreadholic")
                .setTicker("Shreadholic")
                .setContentText("Tracking")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_play, "Play", pPlayIntent).build();


        pauseNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Shreadholic")
                .setTicker("Shreadholic")
                .setContentText("Tracking")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pPauseIntent).build();

        trackResult = new TrackResult();
        trackResult.startTime = TimeUtil.getTimeStamp();
        trackResult.save();

        GlobalApplication.getDefaultSharePreferences().edit().putLong(Constants.SHARE_PREFERENCE.ACTIVE_TRACK_ID, trackResult.id).apply();
        Timber.i("trackResult id: " + trackResult.id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalApplication.getDefaultSharePreferences().edit().putLong(Constants.SHARE_PREFERENCE.ACTIVE_TRACK_ID, -1).apply();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Timber.i("Received Start Foreground Intent ");
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, pauseNotification);
        } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
            Timber.i("Clicked Pause");
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, playNotification);
            //TODO: stop the timer
            stopLocationUpdates();
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, pauseNotification);
            Timber.i("Clicked Play");
            startLocationUpdates();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Timber.i("Received Stop Foreground Intent");
            trackResult.stopTime = TimeUtil.getTimeStamp();
            trackResult.update();

            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Timber.i("Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
        mGoogleApiClient.connect();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
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

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Timber.i("Connected to GoogleApiClient");
        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        //notify the UI to draw line on map
        EventBus.getDefault().post(new LocationUpdateEvent(location));
        //store it into db
        GPSData gpsData = new GPSData();
        gpsData.latLng = new LatLng(location.getLatitude(), location.getLongitude());
        gpsData.speed = location.getSpeed();
        gpsData.timeStamp = TimeUtil.getTimeStamp();
        gpsData.associateTrackResult(trackResult);
        gpsData.save();
//        trackResult.getGPSDatas().add(gpsData);
//        trackResult.save();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Timber.i("Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Timber.i("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}

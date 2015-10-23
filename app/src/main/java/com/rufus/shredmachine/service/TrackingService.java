package com.rufus.shredmachine.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.rufus.shredmachine.R;
import com.rufus.shredmachine.activity.MainActivity;
import com.rufus.shredmachine.utils.Constants;
import com.rufus.shredmachine.utils.GlobalApplication;
import com.rufus.shredmachine.utils.TimeUtil;


import timber.log.Timber;

public class TrackingService extends Service {

    NotificationManager mNotificationManager;

    Notification pauseNotification;
    Notification playNotification;

    ShredLocationManager mShredLocationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices API.
        mShredLocationManager = new ShredLocationManager(this);
        mShredLocationManager.mGoogleApiClient.connect();

        setUpNotificationComponents();
    }

    private void setUpNotificationComponents() {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalApplication.getDefaultSharePreferences().edit().putLong(Constants.SHARE_PREFERENCE.ACTIVE_TRACK_ID, -1).apply();
        mShredLocationManager.stopLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.START_FOREGROUND_ACTION)) {
            Timber.i("Received Start Foreground Intent ");
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, pauseNotification);
        } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
            Timber.i("Clicked Pause");
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, playNotification);
            //TODO: stop the timer
            mShredLocationManager.stopLocationUpdates();
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, pauseNotification);
            Timber.i("Clicked Play");
            mShredLocationManager.startLocationUpdates();
        } else if (intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            Timber.i("Received Stop Foreground Intent");
            mShredLocationManager.trackResult.stopTime = TimeUtil.getTimeStamp();
            mShredLocationManager.trackResult.update();

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
}

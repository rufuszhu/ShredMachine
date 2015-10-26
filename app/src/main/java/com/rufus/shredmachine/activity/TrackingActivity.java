package com.rufus.shredmachine.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.rufus.shredmachine.R;
import com.rufus.shredmachine.ShredMachineApplication;
import com.rufus.shredmachine.adapter.DetectedActivitiesAdapter;
import com.rufus.shredmachine.model.GPSData;
import com.rufus.shredmachine.model.TrackResult;
import com.rufus.shredmachine.model.TrackResult$Table;
import com.rufus.shredmachine.service.ActivityUpdateEvent;
import com.rufus.shredmachine.service.LocationUpdateEvent;
import com.rufus.shredmachine.service.TrackingService;
import com.rufus.shredmachine.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class TrackingActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline line;

    private ArrayList<DetectedActivity> mDetectedActivities;
    private DetectedActivitiesAdapter mAdapter;
    private ListView mDetectedActivitiesListView;


    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;

    @Bind(R.id.nav_view)
    NavigationView navigationView;

    @OnClick(R.id.start)
    void startService() {
        Intent startIntent = new Intent(TrackingActivity.this, TrackingService.class);
        startIntent.setAction(Constants.ACTION.START_FOREGROUND_ACTION);
        startService(startIntent);
    }

    @OnClick(R.id.stop)
    void stopService() {
        if (isTrackingServiceRunning()) {
            Intent stopIntent = new Intent(TrackingActivity.this, TrackingService.class);
            stopIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
            startService(stopIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

//        navigationView.setNavigationItemSelectedListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        LinearLayout headerRoot = (LinearLayout) getLayoutInflater().inflate(R.layout.nav_header_debug, navigationView, false);
        mDetectedActivitiesListView = (ListView) headerRoot.findViewById(R.id.detected_activities_listview);

        navigationView.addHeaderView(headerRoot);

        mDetectedActivities = new ArrayList<>();

        // Set the confidence level of each monitored activity to zero.
        for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
            mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
        }

        mAdapter = new DetectedActivitiesAdapter(this, mDetectedActivities);
        mDetectedActivitiesListView.setAdapter(mAdapter);
    }


    @Override
    public void onResume() {
        Timber.i("onResume");
        drawLineOnMap();
        Timber.d("Is tracking service running? " + isTrackingServiceRunning());
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        ShredMachineApplication.getDefaultEvent().register(this);
    }

    @Override
    public void onStop() {
        ShredMachineApplication.getDefaultEvent().unregister(this);
        super.onStop();
    }

    // This method will be called when a LocationUpdateEvent is posted in ShredLocationManager
    public void onEventMainThread(LocationUpdateEvent event) {
        if (line != null) {
            LatLng newLatLng = new LatLng(event.location.getLatitude(), event.location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(newLatLng, 16)));
            mMap.addMarker(new MarkerOptions()
                    .position(newLatLng)
                    .draggable(true)
                    .title("Speed: " + event.location.getSpeed()));
            List<LatLng> points = line.getPoints();
            points.add(newLatLng);
            line.setPoints(points);
        } else {
            Timber.e("Receive location update event when polyline on map has not been initialized");
        }
    }

    // This method will be called when a ActivityEvent is posted DetectedActivitiesIntentService
    public void onEventMainThread(ActivityUpdateEvent event) {
        mAdapter.updateActivities(event.detectedActivities);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Timber.i("onMapReady");
        mMap = googleMap;
        drawLineOnMap();
    }

    /**
     * Draw the line base on the points in the active TrackResult database,
     * init the empty line if there is no active tracking
     */
    private void drawLineOnMap() {
        if (mMap != null) {
            if (line == null)
                addEmptyLine();

            long activeTrackID = ShredMachineApplication.getDefaultSharePreferences().getLong(Constants.SHARE_PREFERENCE.ACTIVE_TRACK_ID, -1);
            if (activeTrackID != -1) {
                TrackResult trackResult = new Select().from(TrackResult.class).where(Condition.column(TrackResult$Table.ID).is(activeTrackID)).querySingle();
                if (trackResult != null) {
                    ArrayList<LatLng> list = buildLatLngList(trackResult.getGPSDataList());
                    line.setPoints(list);
                } else {
                    Timber.e("Active track result not found in database, id: " + activeTrackID);
                }
            }
        }
    }

    private void addEmptyLine() {
        line = mMap.addPolyline(new PolylineOptions()
                .width(25)
                .color(Color.BLUE)
                .geodesic(true));
    }

    private ArrayList<LatLng> buildLatLngList(List<GPSData> list) {
        ArrayList<LatLng> result = new ArrayList<>();
        for (GPSData gpsData : list) {
            result.add(gpsData.latLng);
        }
        return result;
    }

    private boolean isTrackingServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            Timber.d("Running service: " + service.service.getClassName());
            if (TrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}


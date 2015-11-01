package com.rufus.shredmachine.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rufus.shredmachine.R;
import com.rufus.shredmachine.adapter.PlaceAutocompleteAdapter;
import com.rufus.shredmachine.model.GeofenceData;
import com.rufus.shredmachine.utils.Constants;
import com.rufus.shredmachine.utils.KeyBoard;
import com.rufus.shredmachine.view.geofence.TouchableMapFragment;
import com.rufus.shredmachine.view.geofence.TouchableWrapper;
import com.rufus.shredmachine.view.tracking.CenterCircleView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SetGeoFenceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private LatLngBounds BOUNDS_CURRENT_LOCATION;
    //use canada as bound if current location is not available
    private static final LatLngBounds BOUNDS_CANADA = new LatLngBounds(new LatLng(48.257666, -139.824677), new LatLng(60.133349, -52.900856));
    private static final double ONE_FIFTY_KM_IN_DEGREE = 1.5;
    private static final int ZOOM_LEVEL = 13;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private Location currentLocation;

    private GeofenceData data;
    private double currentRadius;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.center_circle)
    CenterCircleView centerCircleView;

    @Bind(R.id.atv_search_address)
    AutoCompleteTextView atvSearchAddress;

    @Bind(R.id.set_geo_coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @Bind(R.id.left)
    View leftView;

    @Bind(R.id.center)
    View centerView;

    @Bind(R.id.tv_radius)
    TextView tvRadius;

    @OnClick(R.id.current_location_btn)
    void onCurrentClick() {
        moveToCurrentLocation(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_geo_fence);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        TouchableMapFragment mapFragment = (TouchableMapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setTouchListener(new TouchableWrapper.OnTouchListener() {
            @Override
            public void onTouch() {
                centerCircleView.dim();
            }

            @Override
            public void onRelease() {
                centerCircleView.light();
            }
        });

        final Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.geo_fence_snack_bar_tutorial, Snackbar.LENGTH_INDEFINITE);

//        snackbar.setAction(getString(R.string.got_it),
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        snackbar.dismiss();
//                    }
//                });

        snackbar.show();

        data = getIntent().getParcelableExtra(Constants.SHARE_PREFERENCE.GEOFENCE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_geo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = getLayoutInflater();

            final EditText etNickName = (EditText) inflater.inflate(R.layout.dialog_geo_nickname, null);

            if (data != null) {
                etNickName.setText(data.nickName);
                etNickName.setSelection(data.nickName.length() - 1);
                etNickName.selectAll();
            }
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(etNickName)
                    .setTitle("Please enter nick name here")
                    .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (etNickName.getText().toString().isEmpty()) {
                                etNickName.setError("should not be empty");
                            } else {
                                if (data == null) {
                                    GeofenceData newData = new GeofenceData();
                                    newData.latLng = mMap.getCameraPosition().target;
                                    newData.radius = currentRadius;
                                    newData.nickName = etNickName.getText().toString();
                                    newData.zoomLevel = mMap.getCameraPosition().zoom;
                                    newData.save();
                                } else {
                                    data.latLng = mMap.getCameraPosition().target;
                                    data.radius = currentRadius;
                                    data.nickName = etNickName.getText().toString();
                                    data.zoomLevel = mMap.getCameraPosition().zoom;
                                    data.save();
                                }
                                KeyBoard.closeKeyBoard(etNickName);
                                finish();
                            }
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            KeyBoard.closeKeyBoard(etNickName);
                        }
                    });

            builder.create().show();

            etNickName.post(() -> KeyBoard.openKeyBoard(etNickName));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        if (data != null)
            mMap.addCircle(new CircleOptions().center(data.latLng).radius(data.radius).strokeColor(R.color.color_accent).fillColor(Color.BLUE).strokeWidth(20));

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void buildCurrentLocationBound() {
        if (currentLocation != null)
            BOUNDS_CURRENT_LOCATION = new LatLngBounds(new LatLng(currentLocation.getLatitude() - ONE_FIFTY_KM_IN_DEGREE, currentLocation.getLongitude() - ONE_FIFTY_KM_IN_DEGREE), new LatLng(currentLocation.getLatitude() + ONE_FIFTY_KM_IN_DEGREE, currentLocation.getLongitude() + ONE_FIFTY_KM_IN_DEGREE));
    }

    private void buildAutoComplete() {
        if (BOUNDS_CURRENT_LOCATION != null)
            mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_CURRENT_LOCATION, null);
        else
            mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_CANADA, null);

        atvSearchAddress.setAdapter(mAdapter);
        atvSearchAddress.setOnItemClickListener(mAutocompleteClickListener);
    }

    private void moveToCurrentLocation(boolean animate) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            Toast.makeText(this, R.string.err_no_location, Toast.LENGTH_LONG).show();
            if (mGoogleApiClient == null)
                buildGoogleApiClient();
            else
                mGoogleApiClient.connect();
        }

        if (mMap != null && currentLocation != null) {
            if (animate)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), ZOOM_LEVEL));
            else
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), ZOOM_LEVEL));
        } else {
            Toast.makeText(this, R.string.err_no_location, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (data != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(data.latLng, data.zoomLevel));
        } else {
            moveToCurrentLocation(false);
        }

        buildCurrentLocationBound();
        buildAutoComplete();
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //calculate the radius of the circle here
                Projection projection = mMap.getProjection();

                int centerPos = (centerView.getTop() + centerView.getBottom()) / 2;

                Point center = new Point((centerView.getLeft() + centerView.getRight()) / 2, centerPos);
                Point left = new Point(leftView.getRight(), centerPos);

                LatLng latLng = projection.fromScreenLocation(center);
                LatLng latLng2 = projection.fromScreenLocation(left);

//                Timber.i(latLng.toString() + ", " + latLng2.toString());

                Location loc1 = new Location("");
                loc1.setLatitude(latLng.latitude);
                loc1.setLongitude(latLng.longitude);

                Location loc2 = new Location("");
                loc2.setLatitude(latLng2.latitude);
                loc2.setLongitude(latLng2.longitude);
                currentRadius = loc1.distanceTo(loc2) / 1000;
                tvRadius.setText(getString(R.string.radius_km, String.format("%.2f", currentRadius)));
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Timber.i("Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("onConnectionFailed");
        Toast.makeText(this, R.string.err_location_service_connection_fail, Toast.LENGTH_LONG).show();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            Timber.i("Autocomplete item selected: " + item.description);

            atvSearchAddress.setText("");

            KeyBoard.closeKeyBoard(atvSearchAddress);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Timber.i("Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Timber.e("Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)));

            places.release();
        }
    };
}

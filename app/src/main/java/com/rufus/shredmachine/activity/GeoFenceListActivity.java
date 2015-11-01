package com.rufus.shredmachine.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.rufus.shredmachine.R;
import com.rufus.shredmachine.adapter.GeoFenceAdapter;
import com.rufus.shredmachine.model.GeofenceData;
import com.rufus.shredmachine.model.TrackResult;
import com.rufus.shredmachine.model.TrackResult$Table;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GeoFenceListActivity extends AppCompatActivity {

    @Bind(R.id.geofence_list)
    RecyclerView recyclerView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence_list);
        ButterKnife.bind(this);
        final Context context = this;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<GeofenceData> geofenceData = new Select().from(GeofenceData.class).queryList();

        GeoFenceAdapter geoFenceAdapter = new GeoFenceAdapter(geofenceData, new GeoFenceAdapter.GeoFenceClickListener() {
            @Override
            public void onRepositoryClick(GeofenceData geofenceData) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST, dividerPaddingStart, safeIsRtl()));
        recyclerView.setAdapter(geoFenceAdapter);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SetGeoFenceActivity.class);
                startActivity(intent);
            }
        });
    }

}

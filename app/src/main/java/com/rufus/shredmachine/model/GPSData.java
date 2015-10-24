package com.rufus.shredmachine.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

@Table(databaseName = ShredMachineDatabase.NAME)
public class GPSData extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    public long timeStamp;
    @Column
    public LatLng latLng;
    @Column
    public double altitude;
    @Column
    public double speed;
    @Column
    public double bearing;
    @Column
    public double accuracy;

    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "TrackResult_id",
                    columnType = Long.class,
                    foreignColumnName = "id")},
            saveForeignKeyModel = false)
    ForeignKeyContainer<TrackResult> trackResultModelContainer;

    public GPSData() {
        super();
    }

    public GPSData(Location location) {
        super();
        timeStamp = location.getTime();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        altitude = location.getAltitude();
        speed = location.getSpeed();
        bearing = location.getBearing();
        accuracy = location.getAccuracy();
    }


    public void associateTrackResult(TrackResult trackResult) {
        trackResultModelContainer = new ForeignKeyContainer<>(TrackResult.class);
        trackResultModelContainer.setModel(trackResult);
        trackResultModelContainer.put(TrackResult$Table.ID, trackResult.id);
    }


}

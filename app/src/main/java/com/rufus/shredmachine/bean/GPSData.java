package com.rufus.shredmachine.bean;

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
    public double speed;

    @Column
    public LatLng latLng;

    @Column
    public long timeStamp;

    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "TrackResult_id",
                    columnType = Long.class,
                    foreignColumnName = "id")},
            saveForeignKeyModel = false)
    ForeignKeyContainer<TrackResult> trackResultModelContainer;


    public void associateTrackResult(TrackResult trackResult) {
        trackResultModelContainer = new ForeignKeyContainer<>(TrackResult.class);
        trackResultModelContainer.setModel(trackResult);
        trackResultModelContainer.put(TrackResult$Table.ID, trackResult.id);
    }


}

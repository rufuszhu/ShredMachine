package com.rufus.shredmachine.model;

import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by rufus on 2015-10-30.
 */
@Table(databaseName = ShredMachineDatabase.NAME)
public class GeofenceData extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    public String nickName;
    @Column
    public LatLng latLng;
    @Column
    public Double radius;
}

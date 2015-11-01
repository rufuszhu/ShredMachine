package com.rufus.shredmachine.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;

/**
 * Created by rufus on 2015-10-30.
 */
@Table(databaseName = ShredMachineDatabase.NAME)
public class GeofenceData extends BaseModel implements Parcelable{
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    public String nickName;
    @Column
    public LatLng latLng;
    @Column
    public double radius;
    @Column
    public float zoomLevel;

    public GeofenceData(){

    }

    protected GeofenceData(Parcel in) {
        id = in.readLong();
        nickName = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        radius = in.readDouble();
        zoomLevel = in.readFloat();
    }

    public static final Creator<GeofenceData> CREATOR = new Creator<GeofenceData>() {
        @Override
        public GeofenceData createFromParcel(Parcel in) {
            return new GeofenceData(in);
        }

        @Override
        public GeofenceData[] newArray(int size) {
            return new GeofenceData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nickName);
        dest.writeParcelable(latLng, flags);
        dest.writeDouble(radius);
        dest.writeFloat(zoomLevel);
    }
}

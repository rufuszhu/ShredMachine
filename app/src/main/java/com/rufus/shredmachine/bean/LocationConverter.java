package com.rufus.shredmachine.bean;

import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.converter.TypeConverter;

// First type param is the type that goes into the database
// Second type param is the type that the model contains for that field.
@com.raizlabs.android.dbflow.annotation.TypeConverter
public class LocationConverter extends TypeConverter<String, LatLng> {

    @Override
    public String getDBValue(LatLng model) {
        return model == null ? null : String.valueOf(model.latitude) + "," + model.longitude;
    }

    @Override
    public LatLng getModelValue(String data) {
        String[] values = data.split(",");
        if (values.length < 2) {
            return null;
        } else {
            return new LatLng(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
        }
    }
}
package com.rufus.shredmachine.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

@ModelContainer
@Table(databaseName = ShredMachineDatabase.NAME)
public class TrackResult extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String name;

    @Column
    public long startTime;

    @Column
    public long stopTime;

    // needs to be accessible for DELETE
    List<GPSData> gpsDataList;

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "gpsDataList")
    public List<GPSData> getGPSDataList() {
        if (gpsDataList == null) {
            gpsDataList = new Select()
                    .from(GPSData.class)
                    .where(Condition.column(GPSData$Table.TRACKRESULTMODELCONTAINER_TRACKRESULT_ID).is(id))
                    .queryList();
        }
        return gpsDataList;
    }
}

package com.rufus.shredmachine.bean;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = ShredMachineDatabase.NAME, version = ShredMachineDatabase.VERSION)
public class ShredMachineDatabase {
    public static final String NAME = "ShredMachine";

    public static final int VERSION = 1;
}

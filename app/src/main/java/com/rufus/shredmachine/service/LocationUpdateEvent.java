package com.rufus.shredmachine.service;

import android.location.Location;

public class LocationUpdateEvent {
    public final Location location;

    public LocationUpdateEvent(Location location) {
        this.location = location;
    }
}
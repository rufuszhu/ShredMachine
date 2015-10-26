package com.rufus.shredmachine.service;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class ActivityUpdateEvent {
    public final ArrayList<DetectedActivity> detectedActivities;

    public ActivityUpdateEvent(ArrayList<DetectedActivity> detectedActivities) {
        this.detectedActivities = detectedActivities;
    }
}
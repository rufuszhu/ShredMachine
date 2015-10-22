package com.rufus.shredmachine.utils;

import android.text.format.DateFormat;

import java.util.Date;

public final class TimeUtil {
    private TimeUtil() {
        //no object
    }

    public static long getTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static Date getDate(long time) {
        Date date = new Date();
        date.setTime(time);
        return date;
    }

    public static String getDateString(long time) {
        Date date = new Date();
        date.setTime(time);
        //TODO: change to a better format
        return DateFormat.format("dd-MM-yyyy", date).toString();
    }
}

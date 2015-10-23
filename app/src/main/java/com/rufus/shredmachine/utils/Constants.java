package com.rufus.shredmachine.utils;

public class Constants {
    private static String PACKAGE_NAME = GlobalApplication.getInstance().getPackageName();

    public interface ACTION {
        String MAIN_ACTION = PACKAGE_NAME + ".action.main";
        String PAUSE_ACTION = PACKAGE_NAME + ".action.pause";
        String PLAY_ACTION = PACKAGE_NAME + ".action.play";
        String START_FOREGROUND_ACTION = PACKAGE_NAME + ".action.startforeground";
        String STOP_FOREGROUND_ACTION = PACKAGE_NAME + ".action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface SHARE_PREFERENCE {
        String ACTIVE_TRACK_ID = "ACTIVE_TRACK_ID";
    }
}

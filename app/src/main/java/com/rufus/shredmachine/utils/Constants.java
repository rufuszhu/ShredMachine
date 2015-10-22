package com.rufus.shredmachine.utils;

public class Constants {
    private static String PACKAGE_NAME = GlobalApplication.getInstance().getPackageName();

    public interface ACTION {
        public static String MAIN_ACTION = PACKAGE_NAME + ".action.main";
        public static String PAUSE_ACTION = PACKAGE_NAME + ".action.pause";
        public static String PLAY_ACTION = PACKAGE_NAME + ".action.play";
        public static String STARTFOREGROUND_ACTION = PACKAGE_NAME + ".action.startforeground";
        public static String STOPFOREGROUND_ACTION = PACKAGE_NAME + ".action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public interface SHARE_PREFERENCE {
        public static String ACTIVE_TRACK_ID = "ACTIVE_TRACK_ID";
    }
}

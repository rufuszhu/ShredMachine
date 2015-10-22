package com.rufus.shredmachine.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.rufus.shredmachine.BuildConfig;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class GlobalApplication extends Application {
    private static GlobalApplication mInstance;

    public static GlobalApplication getInstance() {
        return mInstance;
    }

    public static SharedPreferences getDefaultSharePreferences() {
        return mInstance.getSharedPreferences(getInstance().getPackageName(), MODE_PRIVATE);
    }

    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }

    public static String getAppPackageName() {
        return mInstance.getPackageName();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        LeakCanary.install(this);
        //init database
        FlowManager.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                //add line number to the tag
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });

            //open debug console in chrome by typing chrome://inspect in the url address
            Stetho.initializeWithDefaults(this);
//        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }
    }
}
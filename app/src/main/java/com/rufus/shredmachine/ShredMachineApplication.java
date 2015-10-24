package com.rufus.shredmachine;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class ShredMachineApplication extends Application {
    private static ShredMachineApplication mInstance;
    private static EventBus eventBus;

    public static ShredMachineApplication getInstance() {
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

    public static EventBus getDefaultEvent(){
        if(eventBus==null)
            eventBus = EventBus.builder().sendNoSubscriberEvent(false).build();

        return eventBus;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

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

            LeakCanary.install(this);
//        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }
    }
}
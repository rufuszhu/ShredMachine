package com.rufus.shredmachine;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.rufus.shredmachine.utils.activityLifecycleObserver.ActivityEvent;
import com.rufus.shredmachine.utils.activityLifecycleObserver.ActivityEventLogger;
import com.rufus.shredmachine.utils.activityLifecycleObserver.ActivityEventProducer;
import com.squareup.leakcanary.LeakCanary;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ShredMachineApplication extends Application {
    private static ShredMachineApplication mInstance;
    private static Observable<ActivityEvent> mActivityEventStream;
    private static EventBus mEventBus;

    @SuppressWarnings("unused")
    public static ShredMachineApplication getInstance() {
        return mInstance;
    }

    @SuppressWarnings("unused")
    public static SharedPreferences getDefaultSharePreferences() {
        return mInstance.getSharedPreferences(getInstance().getPackageName(), MODE_PRIVATE);
    }

    @SuppressWarnings("unused")
    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }

    @SuppressWarnings("unused")
    public static String getAppPackageName() {
        return mInstance.getPackageName();
    }

    @SuppressWarnings("unused")
    public static EventBus getDefaultEvent() {
        if (mEventBus == null)
            mEventBus = EventBus.builder().sendNoSubscriberEvent(false).build();

        return mEventBus;
    }

    @SuppressWarnings("unused")
    public static Observable<ActivityEvent> activityEventStream() {
        return mActivityEventStream;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ActivityEventProducer activityEventProducer = new ActivityEventProducer();
        registerActivityLifecycleCallbacks(activityEventProducer);
        mActivityEventStream = Observable.create(activityEventProducer);

        //init database
        FlowManager.init(this);

        configureDevSettings();
    }

    private void configureDevSettings() {
        if (BuildConfiguration.enable_strict_mode) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        if (BuildConfiguration.enable_logging) {
            Timber.plant(new Timber.DebugTree() {
                //add line number to the tag
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });

            mActivityEventStream.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ActivityEventLogger());
        }

        if (BuildConfiguration.enable_stetho) {
            //open debug console in chrome by typing chrome://inspect in the url address
            Stetho.initializeWithDefaults(this);
        }

        if (BuildConfiguration.enable_leak_canary) {
            LeakCanary.install(this);
        }

        if (BuildConfiguration.enable_crashlytics) {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }
    }

}
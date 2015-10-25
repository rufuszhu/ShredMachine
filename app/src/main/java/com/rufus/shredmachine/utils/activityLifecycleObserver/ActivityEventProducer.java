package com.rufus.shredmachine.utils.activityLifecycleObserver;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.concurrent.ArrayBlockingQueue;

import rx.Observable;
import rx.Subscriber;

public class ActivityEventProducer
        implements Application.ActivityLifecycleCallbacks, Observable.OnSubscribe<ActivityEvent> {

    private ArrayBlockingQueue<ActivityEvent> activityEvents = new ArrayBlockingQueue<>(256, false);
    private boolean anyOneSubscribed;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (!anyOneSubscribed) {
            return;
        }
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setActivityClass(activity.getClass());
        activityEvent.setEventKind(ActivityEvent.ActivityEventKind.CREATED);
        activityEvents.add(activityEvent);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (!anyOneSubscribed) {
            return;
        }
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setActivityClass(activity.getClass());
        activityEvent.setEventKind(ActivityEvent.ActivityEventKind.STARTED);
        activityEvents.add(activityEvent);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (!anyOneSubscribed) {
            return;
        }
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setActivityClass(activity.getClass());
        activityEvent.setEventKind(ActivityEvent.ActivityEventKind.RESUMED);
        activityEvents.add(activityEvent);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (!anyOneSubscribed) {
            return;
        }
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setActivityClass(activity.getClass());
        activityEvent.setEventKind(ActivityEvent.ActivityEventKind.PAUSED);
        activityEvents.add(activityEvent);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (!anyOneSubscribed) {
            return;
        }
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setActivityClass(activity.getClass());
        activityEvent.setEventKind(ActivityEvent.ActivityEventKind.STOPPED);
        activityEvents.add(activityEvent);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (!anyOneSubscribed) {
            return;
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (!anyOneSubscribed) {
            return;
        }
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setActivityClass(activity.getClass());
        activityEvent.setEventKind(ActivityEvent.ActivityEventKind.DESTROYED);
        activityEvents.add(activityEvent);
    }

    @Override
    public void call(Subscriber<? super ActivityEvent> subscriber) {
        anyOneSubscribed = true;
        try {
            while (!subscriber.isUnsubscribed()) {
                ActivityEvent activityEvent = activityEvents.take();
                subscriber.onNext(activityEvent);
            }
        } catch (Exception e) {
            subscriber.onError(e);
        } finally {
            anyOneSubscribed = false;
            activityEvents.clear();
        }
    }
}
package com.rufus.shredmachine.utils.activityLifecycleObserver;

import rx.Subscriber;
import timber.log.Timber;

public class ActivityEventLogger extends Subscriber<ActivityEvent> {


    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onNext(ActivityEvent activityEvent) {
//        Timber.e(activityEvent.toString());
    }
}

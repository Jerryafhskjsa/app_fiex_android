package com.black.net;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public abstract class RequestFunction<T, R> implements Function<Notification<T>, ObservableSource<R>> {
    @Override
    final public ObservableSource<R> apply(Notification<T> notify) throws Exception {
        if (notify.isOnComplete()) {
            afterRequest();
            return Observable.empty();
        }
        if (notify.isOnError()) {
            return Observable.error(notify.getError());
        }
        if (notify.isOnNext()) {
            return applyResult(notify.getValue());
        }
        return Observable.empty();
    }

    public abstract void afterRequest();

    public abstract Observable<R> applyResult(T result) throws Exception;
}

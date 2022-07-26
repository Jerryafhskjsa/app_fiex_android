package com.black.net;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public abstract class RequestFunction2<T, R> implements Function<Notification<T>, ObservableSource<RequestObserveResult<R>>> {
    @Override
    final public ObservableSource<RequestObserveResult<R>> apply(Notification<T> notify) throws Exception {
        afterRequest();
        if (notify.isOnComplete()) {
            return Observable.empty();
        }
        if (notify.isOnError()) {
            RequestObserveResult<R> result = new RequestObserveResult<R>();
            result.error = notify.getError();
            return Observable.just(result);
        }
        if (notify.isOnNext()) {
            RequestObserveResult<R> result = new RequestObserveResult<R>();
            result.value = applyResult(notify.getValue());
            return Observable.just(result);
        }
        return Observable.empty();
    }

    public abstract void afterRequest();

    public abstract R applyResult(T result) throws Exception;
}

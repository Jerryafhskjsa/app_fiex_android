package com.black.base.observe

import io.reactivex.Observable
import io.reactivex.ObservableEmitter

abstract class ObservableBaseDialog<T : ObservableBaseDialog<T>?> {
    protected var dialog: T? = null
    protected var emitter: ObservableEmitter<T>? = null
    fun dialogToObservable(): Observable<T> {
        return Observable.create { e ->
            //Log.e("dialogToObservable", "dialog:" + dialog);
            bindEvents(e)
        }
    }

    abstract fun show(): T
    protected abstract fun bindEvents(emitter: ObservableEmitter<T>?)
}

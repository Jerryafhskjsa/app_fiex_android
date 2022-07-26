package com.black.base.model

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class SuccessObserver<T> : Observer<T> {
    override fun onSubscribe(s: Disposable) {}
    override fun onNext(value: T) {
        onSuccess(value)
    }

    override fun onError(t: Throwable) {}
    override fun onComplete() {}
    abstract fun onSuccess(value: T)
}
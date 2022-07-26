package com.black.base.observe

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class CheckObserver : Observer<Boolean?> {
    override fun onSubscribe(d: Disposable) {}
    override fun onNext(aBoolean: Boolean) {}
    override fun onError(e: Throwable) {}
    override fun onComplete() {}
}
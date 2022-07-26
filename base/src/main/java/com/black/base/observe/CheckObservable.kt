package com.black.base.observe

import io.reactivex.Observable
import io.reactivex.Observer

class CheckObservable : Observable<Boolean?>() {
    override fun subscribeActual(observer: Observer<in Boolean?>) {}
}

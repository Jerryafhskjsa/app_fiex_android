package com.black.base.observe

import io.reactivex.Observable

interface ObserveAlertDialog<T> {
    fun show(): Observable<T>?
    fun dismiss()
    operator fun next()
    fun cancel()
}
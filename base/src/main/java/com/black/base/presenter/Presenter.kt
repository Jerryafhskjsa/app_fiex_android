package com.black.base.presenter

import android.view.View

interface Presenter<M, V : View?> {
    fun getView(model: M, view: V): View
}

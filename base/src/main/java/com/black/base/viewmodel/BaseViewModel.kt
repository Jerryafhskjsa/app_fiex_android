package com.black.base.viewmodel

import android.content.Context

open class BaseViewModel<T>(protected var context: Context) {
    open fun onResume() {}
    open fun onPause() {}
    open fun onStop() {}
    open fun onDestroy() {}

}
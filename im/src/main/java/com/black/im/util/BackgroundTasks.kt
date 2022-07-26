package com.black.im.util

import android.os.Handler

class BackgroundTasks {
    companion object {
        var instance: BackgroundTasks? = null
            private set

        // 需要在主线程中初始化
        fun initInstance() {
            instance = BackgroundTasks()
        }
    }

    val handler = Handler()

    fun runOnUiThread(runnable: Runnable?) {
        handler.post(runnable)
    }

    fun postDelayed(r: Runnable?, delayMillis: Long): Boolean {
        return handler.postDelayed(r, delayMillis)
    }
}
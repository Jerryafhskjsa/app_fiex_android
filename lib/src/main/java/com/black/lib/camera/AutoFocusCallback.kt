package com.black.lib.camera

import android.hardware.Camera
import android.os.Handler

internal class AutoFocusCallback : Camera.AutoFocusCallback {
    companion object {
        private val TAG = AutoFocusCallback::class.java.simpleName
        private const val AUTOFOCUS_INTERVAL_MS = 1500L
    }

    private var autoFocusHandler: Handler? = null
    private var autoFocusMessage = 0
    fun setHandler(autoFocusHandler: Handler?, autoFocusMessage: Int) {
        this.autoFocusHandler = autoFocusHandler
        this.autoFocusMessage = autoFocusMessage
    }

    override fun onAutoFocus(success: Boolean, camera: Camera) {
        if (autoFocusHandler != null) {
            val message = autoFocusHandler?.obtainMessage(autoFocusMessage, success)
            autoFocusHandler?.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS)
            autoFocusHandler = null
        } else {
        }
    }
}
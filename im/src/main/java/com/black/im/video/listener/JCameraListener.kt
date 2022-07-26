package com.black.im.video.listener

import android.graphics.Bitmap

interface JCameraListener {
    fun captureSuccess(bitmap: Bitmap?)
    fun recordSuccess(url: String?, firstFrame: Bitmap?, duration: Long)
}
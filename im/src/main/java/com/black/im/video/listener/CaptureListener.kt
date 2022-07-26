package com.black.im.video.listener

interface CaptureListener {
    fun takePictures()
    fun recordShort(time: Long)
    fun recordStart()
    fun recordEnd(time: Long)
    fun recordZoom(zoom: Float)
    fun recordError()
}
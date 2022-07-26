package com.black.base.share

interface ShareResultListener {
    fun onError(type: Int, message: Any?)
    fun onCancel()
    fun onComplete()
}
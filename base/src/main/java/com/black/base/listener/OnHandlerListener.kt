package com.black.base.listener

interface OnHandlerListener<T> {
    fun onCancel(widget: T)
    fun onConfirm(widget: T)
}

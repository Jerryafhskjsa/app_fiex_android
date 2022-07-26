package com.black.base.util

object FryingSecret {
    init {
        System.loadLibrary("FryingSecret")
    }

    external fun getSecret(): String?
}
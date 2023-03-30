package com.black.frying.contract.viewmodel.model

import com.black.base.util.CookieUtil
import com.black.frying.FryingApplication

data class FuturesCoinPair(val coinSrc: String, val coinTar: String) {
    companion object {
        fun load(): FuturesCoinPair? {
            val context = FryingApplication.instance()
            val uPair = CookieUtil.getCurrentFutureUPair(context)
            return if (uPair.isNullOrEmpty()) {
                null
            } else {
                val split = uPair.split("_")
                FuturesCoinPair(split[0], split[1])
            }
        }
    }

    fun reload(): FuturesCoinPair? {
        return load()
    }

    fun source(): String {
        return "${coinSrc}_$coinTar"
    }
    fun display(): String {
        return "$coinSrc/$coinTar"
    }
}
package com.black.base.model.money

import android.content.Context

class CloudPowerHoldRecord {
    var coinType: String? = null
    var amount: Double? = null
    var totalInterest: Double? = null
    var endTime: Long? = null
    var status: Int? = null
    var miningTime: Long? = null
    var day: Int? = null
    val statusCode: Int
        get() = status ?: 0

    fun getStatusText(context: Context?): String {
        return when (statusCode) {
            3 -> "收益中"
            4 -> "已失效"
            else -> "未生效"
        }
    }
}
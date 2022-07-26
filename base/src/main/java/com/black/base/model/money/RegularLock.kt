package com.black.base.model.money

import android.content.Context
import com.black.base.R

class RegularLock {
    var id: String? = null
    var coinType: String? = null
    var amount: Double? = null
    var annualrate: String? = null
    var days: String? = null
    var interest: Double? = null
    var createdTime: Long? = null
    var updateTime: Long? = null
    var endTime: Long? = null
    var status: Int? = null
    var defaultRate: Double? = null
    var defaultAmount: Double? = null
    fun getStatusText(context: Context): String {
        val nullAmount = context.resources.getString(R.string.number_default)
        return if (status == null) {
            nullAmount
        } else when (status) {
            4 -> "已违约"
            5 -> "已完成"
            else -> "计息中"
        }
    }

    companion object {
        fun isEnd(regularLock: RegularLock?): Boolean {
            if (regularLock?.status == null) {
                return false
            }
            val status = regularLock.status!!
            return status == 4 || status == 5
        }
    }
}
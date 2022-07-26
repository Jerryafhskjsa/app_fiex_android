package com.black.base.model.money

import android.content.Context

class DemandLock {
    var id: String? = null
    var coinType: String? = null
    var amount: Double? = null
    var createTime: Long? = null
    var currentRate: Double? = null
    var nextRate: Double? = null
    var lastInterestAmount: Double? = null
    var nextInterestAmount: Double? = null
    var totalInterestAmount: Double? = null
    var remainingDay: Int? = null
    var distributionCoinType: String? = null

    companion object {
        fun getStatusText(context: Context?, status: Int): String {
            return if (status == 1) {
                "计息中"
            } else {
                "已取出"
            }
        }
    }
}
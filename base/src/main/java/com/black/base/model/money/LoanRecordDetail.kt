package com.black.base.model.money

import android.content.Context
import com.black.base.R

class LoanRecordDetail protected constructor() {
    var createTime: Long? = null
    var endTime: Long? = null
    var numberDays: Int? = null
    var burstBorrowPrice: Double? = null
    var burstMortgagePrice: Double? = null
    var burstRiskRate: Double? = null
    var borrowAmount: Double? = null
    var borrowCoinType: String? = null
    var mortgageCoinType: String? = null
    var mortgageAmount: Double? = null
    var returnAmount: Double? = null
    var interest: Double? = null
    var defaultAmount: Double? = null
    var burstAmount: Double? = null
    var status: Int? = null
    var rate: Double? = null
    var defaultRate: Double? = null
    fun getStatusText(context: Context): String {
        if (status != null) {
            when (status) {
                0 -> return "借款失败"
                1 -> return "计息中"
                2 -> return "已到期"
                3 -> return "已逾期"
                4, 5, 11 -> return "已还款"
                12 -> return "已爆仓"
            }
        }
        return context.resources.getString(R.string.number_default)
    }

    val statusInt: Int
        get() = status ?: Int.MAX_VALUE

    val isDoing: Boolean
        get() = statusInt < 4
}
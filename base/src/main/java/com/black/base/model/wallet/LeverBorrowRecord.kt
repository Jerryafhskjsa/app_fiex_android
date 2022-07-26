package com.black.base.model.wallet

import android.content.Context
import com.black.base.R

class LeverBorrowRecord {
    var id: String? = null
    var userId: String? = null
    var pair: String? = null
    var coinType: String? = null
    var amount: Double? = null
    var createTime: Long? = null
    var operationType: String? = null
    var status: Int? = null
    fun getTypeText(context: Context): String {
        return if (operationType == null) {
            context.resources.getString(R.string.number_default)
        } else when (operationType) {
            "BORROW" -> "借币"
            "REPAYMENT" -> "还币"
            "BURST" -> "爆仓"
            else -> context.resources.getString(R.string.number_default)
        }
    }
}
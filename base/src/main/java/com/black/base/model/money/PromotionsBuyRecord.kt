package com.black.base.model.money

import android.content.Context
import com.black.base.R

class PromotionsBuyRecord {
    var coinAmount: Double? = null
    var payCoin: String? = null
    var payAmount: Double? = null
    var needAmount: Double? = null
    var price: Double? = null
    var createTime: Long? = null
    var status: Int? = null
    fun getStatusText(context: Context): String {
        return when (if (status == null) -1 else status!!) {
            3 -> context.getString(R.string.purchase_reqeuest)
            4 -> context.getString(R.string.purchase_done)
            5 -> context.getString(R.string.purchase_failed)
            1, 2 -> context.getString(R.string.purchase_doing)
            else -> context.getString(R.string.number_default)
        }
    }
}
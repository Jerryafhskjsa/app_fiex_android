package com.black.base.model.money

import android.content.Context
import com.black.base.R
import java.util.*

class PromotionsRecord {
    var amount: Double? = null
    var price: Double? = null
    var coinType: String? = null
    var distributionCoinType: String? = null
    var id: String? = null
    var createdTime: Date? = null
    var distributionAmount: Double? = null
    var status: Int? = null
    fun getStatusText(context: Context): String {
        return if (status != null && status == 2) context.getString(R.string.rush_success) else context.getString(R.string.rush_failed)
    }
}
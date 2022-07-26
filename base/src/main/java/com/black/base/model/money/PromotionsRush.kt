package com.black.base.model.money

import android.content.Context
import com.black.base.R

//发售促销
class PromotionsRush {
    var id: Long? = null
    var name: String? = null
    var coinType: String? = null
    var totalFinancing: Double? = null
    var nowFinancing: Double? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var imageUrl: String? = null
    var introduction: String? = null
    var userOrderCount: Int? = null
    var userFinancingAmount: Double? = null
    var balance: String? = null
    var progress: String? = null
    var status //3 结束 2 完成 1 正在进行 0 未开始
            : Int? = null
    var distributionCoinType: String? = null
    var price: Double? = null
    var userAmount: Double? = null
    var userTotalAmout: Double? = null
    var thisTime: Long? = null
    val statusCode: Int
        get() = status ?: 3

    fun getStatusDisplay(context: Context): String {
        return when (statusCode) {
            1 -> context.getString(R.string.promotions_doing)
            2 -> context.getString(R.string.promotions_done)
            3 -> context.getString(R.string.promotions_finish)
            else -> context.getString(R.string.promotions_not_start)
        }
    }
}
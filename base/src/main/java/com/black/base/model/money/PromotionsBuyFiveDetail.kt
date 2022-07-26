package com.black.base.model.money

import android.content.Context
import com.black.base.R
import com.black.util.NumberUtil
import java.util.*

class PromotionsBuyFiveDetail {
    var purchaseId: String? = null
    var name: String? = null
    var coinType: String? = null
    var signUrl: String? = null
    var bannerUrl: String? = null
    var infoImageUrl: String? = null
    var nowAmount: Double? = null
    var totalAmount: Double? = null
    var type: Int? = null
    var status //3 结束 2 完成 1 正在进行 0 未开始
            : Int? = null
    var openPrice: String? = null
    var price: Double? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var systemTime: Long? = null
    var personNum: Int? = null
    var precisions: Double? = null
    var minAmount: Double? = null
    var maxAmount: Double? = null
    var supportCoin: ArrayList<SupportCoin?>? = null
    var officialUrl: String? = null
    var whiteUrl: String? = null
    var ruleText: String? = null
    val priceDisplay: String
        get() {
            val sb = StringBuilder()
            if (supportCoin != null) {
                for (i in supportCoin!!.indices) {
                    val coin = supportCoin!![i]
                    if (sb.isEmpty()) {
                        sb.append(coin?.priceDisplay)
                    } else {
                        sb.append(" / ").append(coin?.priceDisplay)
                    }
                }
            }
            return sb.toString()
        }

    val statusCode: Int
        get() = status ?: 3

    val nowAmountResult: Double?
        get() {
            if (nowAmount == null || minAmount == null) {
                return null
            }
            return if (nowAmount == 0.0 || minAmount == 0.0) {
                0.0
            } else nowAmount!! / minAmount!!
        }

    val totalAmountResult: Double?
        get() {
            if (totalAmount == null || minAmount == null) {
                return null
            }
            return if (totalAmount == 0.0 || minAmount == 0.0) {
                0.0
            } else totalAmount!! / minAmount!!
        }

    fun getCopyHintText(context: Context): String {
        val nullAmount = context.resources.getString(R.string.number_default)
        return String.format("最小申购份数%s，最大申购份数%s",
                if (minAmount == null) nullAmount else "1",
                if (minAmount == null || maxAmount == null) nullAmount else if (minAmount == 0.0 || maxAmount == 0.0) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(maxAmount!! / minAmount!!, 6, 0, 2))
    }
}
package com.black.base.model.c2c

import android.content.Context
import com.black.base.R
import com.black.base.model.BaseAdapterItem
import com.black.util.NumberUtil
import java.math.RoundingMode

open class C2COrder : BaseAdapterItem() {
    companion object {
        const val ORDER_DOING = 0
        const val ORDER_BUY = "BID"
        const val ORDER_SELL = "ASK"
    }

    var id: Long? = null
    var coinType: String? = null
    var direction //购买出售
            : String? = null
    var amount: Double? = 0.0
    var price: Double? = 0.0
    var userId: String? = null
    var merchantId: Long = 0
    var status //0:进行中；1：已经完成；-1:已取消；2:审核中；3:审核通过；4:用户已付款；-2:超时已取消；-3:审核不通过
            = 0
    var createTime: Long? = null
    var updateTime: Long? = null
    var merchantName //商户名
            : String? = null
    var userName //商户名
            : String? = null
    var merchantRealName //下单用户真实姓名
            : String? = null
    var merchant // 在该订单中扮演的角色  商户/用户  true/false
            = false

    override fun getType(): Int {
        return C2C_ORDER
    }

    fun getStatusDisplay(context: Context): String {
        when (status) {
            0 -> return context.getString(R.string.c2c_order_status_01)
            1 -> return context.getString(R.string.c2c_order_status_02)
            2 -> return context.getString(R.string.c2c_order_status_03)
            3 -> return context.getString(R.string.c2c_order_status_04)
            4 -> return context.getString(R.string.c2c_order_status_05)
            5 -> return context.getString(R.string.c2c_order_status_10)
            -1 -> return context.getString(R.string.c2c_order_status_06)
            -2 -> return context.getString(R.string.c2c_order_status_07)
            -3 -> return context.getString(R.string.c2c_order_status_08)
            -4 -> return context.getString(R.string.c2c_order_status_09)
            -5 -> return context.getString(R.string.c2c_order_status_11)
            -6 -> return context.getString(R.string.c2c_order_status_12)
            -7 -> return context.getString(R.string.c2c_order_status_07)
        }
        return ""
    }

    fun getDirectionDisplay(context: Context): String {
        if (ORDER_BUY.equals(direction, ignoreCase = true)) {
            return context.getString(R.string.c2c_buy)
        } else if (ORDER_SELL.equals(direction, ignoreCase = true)) {
            return context.getString(R.string.c2c_sell)
        }
        return ""
    }

    fun getAmountDisplay(context: Context?): String {
        return NumberUtil.formatNumberNoGroup(amount, RoundingMode.FLOOR, 2, 4)
    }

    fun getTotalMoneyDisplay(context: Context?): String {
        return NumberUtil.formatNumberNoGroupHardScale(if (amount == null || price == null) null else amount!! * price!!, RoundingMode.FLOOR, 2)
    }

    val isEnd: Boolean
        get() = status < 0 || status == 1

    val isBuy: Boolean
        get() = ORDER_BUY == direction
}

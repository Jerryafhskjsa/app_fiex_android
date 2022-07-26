package com.black.base.model.socket

import android.content.ContentValues
import com.black.util.CommonUtil
import java.util.*

class QuotationDealNew {
    var pair: String? = null
    var p //价格 price
            : String? = null
    var a //交易量 amount
            = 0.0
    var t //时间
            : Long = 0
    var d //方向 direction
            : String? = null

    val contentValues: ContentValues
        get() {
            val values = ContentValues()
            values.put("createdTime", t)
            values.put("dealAmount", a)
            values.put("formattedPrice", p)
            values.put("pair", pair)
            values.put("tradeDealDirection", d)
            return values
        }

    fun toTradeOrder(): TradeOrder {
        val tradeOrder = TradeOrder()
        tradeOrder.createdTime = t
        tradeOrder.dealAmount = a
        tradeOrder.formattedPrice = p
        tradeOrder.pair = pair
        val price = CommonUtil.parseDouble(p)
        tradeOrder.price = price ?: 0.0
        tradeOrder.tradeDealDirection = d
        return tradeOrder
    }

    companion object {
        val COMPARATOR: Comparator<QuotationDealNew?> = Comparator { o1, o2 ->
            if (o1 == null || o2 == null) {
                0
            } else o2.t.compareTo(o1.t)
        }
    }
}
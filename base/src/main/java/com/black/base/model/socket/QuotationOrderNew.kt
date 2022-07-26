package com.black.base.model.socket

import android.content.ContentValues
import android.database.Cursor
import com.black.util.CommonUtil

class QuotationOrderNew {
    var pair: String? = null
    var p //价格 price
            : String? = null
    var a //交易量 amount
            = 0.0
    var v //交易额 volume
            = 0.0
    var d //方向 direction
            : String? = null

    constructor()
    constructor(cursor: Cursor) {
        pair = cursor.getString(cursor.getColumnIndex("pair"))
        p = cursor.getString(cursor.getColumnIndex("price"))
        a = cursor.getDouble(cursor.getColumnIndex("exchangeAmount"))
        d = cursor.getString(cursor.getColumnIndex("orderType"))
    }

    val key: String
        get() = pair + p + d

    val contentValues: ContentValues
        get() {
            val values = ContentValues()
            values.put("exchangeAmount", a)
            values.put("orderType", d)
            values.put("pair", pair)
            values.put("price", p)
            return values
        }

    fun toTradeOrder(): TradeOrder {
        val tradeOrder = TradeOrder()
        tradeOrder.pair = pair
        tradeOrder.priceString = p
        val price = CommonUtil.parseDouble(p)
        tradeOrder.price = price ?: 0.0
        tradeOrder.exchangeAmount = a
        tradeOrder.orderType = d
        return tradeOrder
    }
}

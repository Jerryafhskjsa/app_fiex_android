package com.black.base.model.socket

import android.content.ContentValues

class PairStatusNew {
    var pair: String? = null
    var o = 0.0
    var c //最新价
            = 0.0
    var h //24小时最高价
            = 0.0
    var l //24小时最低价
            = 0.0
    var a //24小时成交量
            = 0.0
    var r //24小时涨跌
            = 0.0

    val contentValues: ContentValues
        get() {
            val values = ContentValues()//是一个HashMap，为写入数据库使用
            values.put("currentPrice", c)
            values.put("maxPrice", h)
            values.put("minPrice", l)
            values.put("pair", pair)
            values.put("priceChangeSinceToday", r)
            values.put("totalAmount", a)
            return values
        }

    fun toPairStatus(): PairStatus {
        val pairStatus = PairStatus()
        pairStatus.pair = pair
        pairStatus.currentPrice = c
        pairStatus.maxPrice = h
        pairStatus.minPrice = l
        pairStatus.totalAmount = a
        pairStatus.priceChangeSinceToday = r
        return pairStatus
    }

    fun copyValues(pairStatus: PairStatus?) {
        if (pairStatus == null) {
            return
        }
        pairStatus.pair = pair
        pairStatus.currentPrice = c
        pairStatus.maxPrice = h
        pairStatus.minPrice = l
        pairStatus.totalAmount = a
        pairStatus.priceChangeSinceToday = r
    }

    companion object {
        fun copyValues(oldObj: PairStatusNew?, newObj: PairStatusNew?) {
            if (oldObj == null || newObj == null) {
                return
            }
            oldObj.pair = newObj.pair
            oldObj.o = newObj.o
            oldObj.c = newObj.c
            oldObj.h = newObj.h
            oldObj.l = newObj.l
            oldObj.a = newObj.a
            oldObj.r = newObj.r
        }
    }
}

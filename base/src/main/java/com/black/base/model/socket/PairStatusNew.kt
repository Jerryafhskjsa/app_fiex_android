package com.black.base.model.socket

import android.content.ContentValues

/**
 * 行情下发数据结构
 */
class PairStatusNew {
    var s:String? = null
    var o:String? = "0.0" //open 开盘价
    var c:String? //最新价
            = "0.0"
    var h:String? //24小时最高价
            = "0.0"
    var l:String? //24小时最低价
            = "0.0"
    var a:String? //24小时成交量
            = "0.0"
    var r:String? //24小时涨跌
            = "0.0"
    var v:String? = "0.0"//成交额


    val contentValues: ContentValues
        get() {
            val values = ContentValues()//是一个HashMap，为写入数据库使用
            values.put("currentPrice", c)
            values.put("maxPrice", h)
            values.put("minPrice", l)
            values.put("pair", s)
            values.put("priceChangeSinceToday", r)
            values.put("totalAmount", a)
            values.put("volume",v)
            return values
        }

    fun toPairStatus(): PairStatus {
        val pairStatus = PairStatus()
        pairStatus.pair = s
        pairStatus.currentPrice = c?.toDouble()!!
        pairStatus.maxPrice = h?.toDouble()!!
        pairStatus.minPrice = l?.toDouble()!!
        pairStatus.totalAmount = a?.toDouble()!!
        pairStatus.priceChangeSinceToday = r?.toDouble()!!
        pairStatus.tradeVolume = v
        return pairStatus
    }

    fun copyValues(pairStatus: PairStatus?) {
        if (pairStatus == null) {
            return
        }
        pairStatus.pair = s
        pairStatus.currentPrice = c?.toDouble()!!
        pairStatus.maxPrice = h?.toDouble()!!
        pairStatus.minPrice = l?.toDouble()!!
        pairStatus.totalAmount = a?.toDouble()!!
        pairStatus.priceChangeSinceToday = r?.toDouble()!!
    }

    companion object {
        fun copyValues(oldObj: PairStatusNew?, newObj: PairStatusNew?) {
            if (oldObj == null || newObj == null) {
                return
            }
            oldObj.o = newObj.o
            oldObj.c = newObj.c
            oldObj.h = newObj.h
            oldObj.l = newObj.l
            oldObj.a = newObj.a
            oldObj.r = newObj.r
        }
    }
}

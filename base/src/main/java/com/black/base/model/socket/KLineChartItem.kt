package com.black.base.model.socket

import com.black.base.util.KLineUtil

class KLineChartItem {
    var time: Long = 0
    var isAddData = false
    var isNullData = false
    var MA5 = 0.0
    var MA10 = 0.0
    var MA20 = 0.0
    var MA30 = 0.0
    var MA60 = 0.0
    var KMax = 0.0
    var KMin = 0.0
    var MAMax = 0.0
    var MAMin = 0.0
    var BOLL = 0.0
    var UB = 0.0
    var LB = 0.0
    var BOLLMax = 0.0
    var BOLLMin = 0.0
    var VOL: Double = 0.0
    var VOLMA5 = 0.0
    var VOLMA10 = 0.0
    var VOLMA20 = 0.0
    var VOLMA60 = 0.0
    var VOLMax = 0.0
    var VOLMin = 0.0
    var MACD = 0.0
    var DIF = 0.0
    var DEA = 0.0
    var MACDMax = 0.0
    var MACDMin = 0.0
    var K = 0.0
    var D = 0.0
    var J = 0.0
    var KDJMax = 0.0
    var KDJMin = 0.0
    var RSI = 0.0
    var WR = 0.0
    var high: Double = 0.0
    var low: Double = 0.0
    var `in`: Double = 0.0
    var out: Double = 0.0
    var x //x坐标
            = 0f
    var highY = 0f
    var lowY = 0f
    var inY = 0f
    var outY = 0f
    var ma5Y = 0f
    var ma10Y = 0f
    var ma30Y = 0f
    var bollY = 0f
    var ubY = 0f
    var lbY = 0f
    var volY = 0f
    var volma5Y = 0f
    var volma10Y = 0f
    var macdY = 0f
    var difY = 0f
    var deaY = 0f
    var kY = 0f
    var dY = 0f
    var jY = 0f
    var rsiY = 0f
    var wrY = 0f

    constructor(kLineItem: KLineItem?) {
        if (kLineItem == null) {
            isNullData = true
            return
        }
        time = kLineItem.t ?: 0
        VOL = kLineItem.a
        high = kLineItem.h
        low = kLineItem.l
        `in` = kLineItem.o
        out = kLineItem.c
    }

    constructor(time: Long, VOL: Double, high: Double, low: Double, `in`: Double, out: Double) {
        this.time = time
        this.VOL = VOL
        this.high = high
        this.low = low
        this.`in` = `in`
        this.out = out
    }

    fun resetMaxValues() {
        KMax = KLineUtil.max(`in`, out, high, low)
        KMin = KLineUtil.min(`in`, out, high, low)
        //计算最值
        MAMax = KLineUtil.max(KMax, MA5, MA10, MA30)
        MAMin = KLineUtil.min(KMin, MA5, MA10, MA30)
        BOLLMax = KLineUtil.max(KMax, BOLL, UB, LB)
        BOLLMin = KLineUtil.min(KMin, BOLL, UB, LB)
        VOLMax = KLineUtil.max(VOL, VOLMA5, VOLMA10)
        VOLMin = KLineUtil.min(VOL, VOLMA5, VOLMA10)
        MACDMax = KLineUtil.max(MACD, DIF, DEA)
        MACDMin = KLineUtil.min(MACD, DIF, DEA)
        KDJMax = KLineUtil.max(K, D, J)
        KDJMin = KLineUtil.min(K, D, J)
    }

    constructor(time: Long, MA5: Double, MA10: Double, MA30: Double, BOLL: Double, UB: Double, LB: Double, VOL: Double, VOLMA5: Double, VOLMA10: Double, MACD: Double, DIF: Double, DEA: Double, k: Double, d: Double, j: Double, RSI: Double, WR: Double, high: Double, low: Double, `in`: Double, out: Double) {
        this.time = time
        this.MA5 = MA5
        this.MA10 = MA10
        this.MA30 = MA30
        this.BOLL = BOLL
        this.UB = UB
        this.LB = LB
        this.VOL = VOL
        this.VOLMA5 = VOLMA5
        this.VOLMA10 = VOLMA10
        this.MACD = MACD
        this.DIF = DIF
        this.DEA = DEA
        K = k
        D = d
        J = j
        this.RSI = RSI
        this.WR = WR
        this.high = high
        this.low = low
        this.`in` = `in`
        this.out = out
        val maxShadow = Math.max(Math.max(`in`, out), Math.max(high, low))
        val minShadow = Math.min(Math.min(`in`, out), Math.min(high, low))
        //计算最值
        MAMax = Math.max(Math.max(Math.max(maxShadow, MA5), MA10), MA30)
        MAMin = Math.min(Math.min(Math.min(minShadow, MA5), MA10), MA30)
        BOLLMax = Math.max(Math.max(Math.max(maxShadow, BOLL), UB), LB)
        BOLLMin = Math.min(Math.min(Math.min(minShadow, BOLL), UB), LB)
        VOLMax = Math.max(Math.max(VOL, VOLMA5), VOLMA10)
        VOLMin = Math.min(Math.min(VOL, VOLMA5), VOLMA10)
        MACDMax = Math.max(Math.max(MACD, DIF), DEA)
        MACDMin = Math.min(Math.min(MACD, DIF), DEA)
        KDJMax = Math.max(Math.max(K, D), J)
        KDJMin = Math.min(Math.min(K, D), J)
    }
}
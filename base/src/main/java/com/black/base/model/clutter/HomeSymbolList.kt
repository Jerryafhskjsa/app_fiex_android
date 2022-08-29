package com.black.base.model.clutter

import java.util.*

/**
 * [{
"symbol":"BTC_USDT",
"tradeSwitch":true,
"state":0,
"buyCoin":"USDT",
"sellCoin":"BTC",
"buyCoinPrecision":6,
"buyCoinDisplayPrecision":4,
"sellCoinPrecision":6,
"sellCoinDisplayPrecision":4,
"quantityPrecision":6,
"pricePrecision":2,
"supportOrderType":"1,2",
"supportTimeInForce":"1,2,3,4",
"minPrice":null,
"minQty":"0.000001",
"minNotional":null,
"multiplierDown":"0.95",
"multiplierUp":"1.05",
"makerFee":"0.0002",
"takerFee":"0.0005",
"marketTakeBound":"0.001",
"depthPrecisionMerge":5,
"onboardDate":null,
"setType":1
}]
 */
class HomeSymbolList {
        var symbol: String? = null
        var tradeSwitch: String? = null
        var state: String? = null
        var buyCoin: String? = null
        var sellCoin: String? = null
        var buyCoinPrecision: String? = null
        var buyCoinDisplayPrecision: String? = null
        var sellCoinPrecision: String? = null
        var quantityPrecision: String? = null
        var pricePrecision: String? = null
        var supportOrderType: String? = null
        var supportTimeInForce: String? = null
        var minPrice: String? = null
        var minQty: String? = null
        var minNotional: String? = null
        var multiplierDown: String? = null
        var multiplierUp: String? = null
        var makerFee: String? = null
        var takerFee: String? = null
        var marketTakeBound: String? = null
        var depthPrecisionMerge: String? = null
        var onboardDate: String? = null
        var setType: Int? = null
        var hot:Boolean? = null
}

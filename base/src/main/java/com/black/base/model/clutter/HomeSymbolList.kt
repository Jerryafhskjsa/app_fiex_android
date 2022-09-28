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
        var symbol: String? = null//交易对
        var tradeSwitch: String? = null//交易对开关
        var state: String? = null//状态
        var buyCoin: String? = null//买方币种
        var sellCoin: String? = null//卖方币种
        var buyCoinPrecision: String? = null//买方币种精度
        var buyCoinDisplayPrecision: String? = null//买方币种显示精度
        var sellCoinPrecision: String? = null//卖方币种精度
        var quantityPrecision: String? = null//卖方币种显示精度
        var pricePrecision: String? = null//价格精度
        var supportOrderType: String? = null//支持订单类型
        var supportTimeInForce: String? = null//支持有效方式
        var minPrice: String? = null//最小价格
        var minQty: String? = null//最小数量
        var minNotional: String? = null//最小名义价值
        var multiplierDown: String? = null//限价卖单下限百分比
        var multiplierUp: String? = null//限价买单价格上限百分比
        var makerFee: String? = null//maker手续费
        var takerFee: String? = null//taker手续费
        var marketTakeBound: String? = null//市价单最多价格偏离
        var depthPrecisionMerge: String? = null//盘口精度合并
        var onboardDate: String? = null//上线时间
        var setType: Int? = null//交易区类型
        var hot:Boolean? = null//热搜
}

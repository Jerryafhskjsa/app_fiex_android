package com.black.base.model.future

data class SymbolBean(
    val baseCoin: String,//标的资产
    val baseCoinDisplayPrecision: Int,//标的币种显示精度
    val baseCoinPrecision: Int,//标的币种精度
    val cnName: String,
    val contractSize: String,//合约乘数（面值）
    val contractType: String,//合约类型，永续，交割
    val depthPrecisionMerge: Int, //盘口精度合并
    val enName: String,
    val initLeverage: Int,//初始杠杆倍数
    val initPositionType: String,
    val labels: List<String>,//标签
    val liquidationFee: String, //强平手续费
    val makerFee: String,//maker手续费
    val marketTakeBound: String,
    val maxEntrusts: Int,//最多open条件单
    val maxNotional: String,
    val maxOpenOrders: Int,//最多open订单
    val minNotional: String,//最小名义价值
    val minPrice: Any, //最小价格
    val minQty: String, //最小数量
    val minStepPrice: String,
    val multiplierDown: String,//限价卖单下限百分比
    val multiplierUp: String,//限价买单价格上限百分比
    val onboardDate: Long,//上线时间
    val pricePrecision: Int,//价格精度
    val quantityPrecision: Int,//数量精度
    val quoteCoin: String,//报价资产
    val quoteCoinDisplayPrecision: Int,//报价币种显示精度
    val quoteCoinPrecision: Int,//报价币种精度
    val state: Int,//状态
    val supportEntrustType: String,//支持计划委托类型
    val supportOrderType: String,//支持订单类型
    val supportPositionType: String,
    val supportTimeInForce: String, //支持有效方式
    val symbol: String,
    val takerFee: String,//taker手续费
    val tradeSwitch: Boolean,//交易对开关
    val underlyingType: String//标的类型，币本位，u本位
)
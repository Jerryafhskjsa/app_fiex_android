package com.black.base.model.socket


//委托订单
class TradeOrderFiex{
    /*fiex********************/
    var avgPrice:String? = null//成交均价 == dealAvgPrice
    var clientOrderId:String? = null//自定义订单I
    var createdTime: Long? = null//创建时间
    var dealQty:String? = null
    var forceClose:Boolean? = false//是否是全平订单
    var marginFrozen:String? = null//占用保证金 == frozenAmountByOrder
    var orderId:String? = null//订单id == id
    var orderSide:String? = null//订单方向 == direction
    var orderType: String? = null//订单类型
    var origQty:String? = null//数量(张)
    var price: String? = null//委托价格
    var sourceId:String? = null//条件触发id
    var state:String? = null//订单状态 = status
    var symbol:String? = null//交易对 = pair
    var timeInForce:String? = null//有效类型
    var triggerProfitPrice:String? = null//止盈触发价
    var triggerStopPrice:String? = null//止损触发价

    var weightPercent //权重占比，绘制挂单进度条使用
            = 0.0
    var beforeAmount //自身和前面所有挂单数量，扫单使用
            = 0.0
    var executedQty: String? = null
        get() {
            if (dealQty != null) {
                field = dealQty
            }
            return field
        }
    /*fiex********************/
}

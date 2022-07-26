package com.black.base.model.socket

//委托订单对
class TradeOrderPair {
    var order //序号
            = 0
    var bidOrder //买单
            : TradeOrder? = null
    var askOrder //卖单
            : TradeOrder? = null
}

package com.black.base.model.socket

import java.util.*

//委托订单对列表
class TradeOrderPairList {
    var bidOrderList //买单列表
            : ArrayList<TradeOrder?>? = null
    var askOrderList //卖单列表
            : ArrayList<TradeOrder?>? = null
}